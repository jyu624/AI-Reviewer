package top.yumbo.ai.reviewer.application.hackathon.cli.parser;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import top.yumbo.ai.reviewer.application.hackathon.cli.dto.TeamSubmission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 团队提交文件解析器
 * 支持 CSV, JSON, YAML 格式
 */
@Slf4j
public class TeamSubmissionParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 解析团队提交文件
     */
    public static List<TeamSubmission> parse(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            throw new IOException("文件不存在: " + filePath);
        }

        String fileName = filePath.getFileName().toString().toLowerCase();
        String content = Files.readString(filePath);

        if (fileName.endsWith(".csv")) {
            return parseCSV(content);
        } else if (fileName.endsWith(".json")) {
            return parseJSON(content);
        } else if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            return parseYAML(content);
        } else if (fileName.endsWith(".txt")) {
            return parseSimpleText(content);
        } else {
            throw new IllegalArgumentException("不支持的文件格式: " + fileName + "。支持的格式: .csv, .json, .yaml, .txt");
        }
    }

    /**
     * 解析 CSV 格式
     * 格式: team_name,repo_url,contact_email,submission_time
     */
    private static List<TeamSubmission> parseCSV(String content) {
        List<TeamSubmission> submissions = new ArrayList<>();
        String[] lines = content.split("\\r?\\n");

        // 跳过标题行（如果存在）
        int startLine = lines.length > 0 && lines[0].toLowerCase().contains("team") ? 1 : 0;

        for (int i = startLine; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split(",");
            if (parts.length >= 2) {
                TeamSubmission submission = TeamSubmission.builder()
                    .teamName(parts[0].trim())
                    .repoUrl(parts[1].trim())
                    .contactEmail(parts.length > 2 ? parts[2].trim() : "")
                    .submissionTime(parts.length > 3 ? parseDateTime(parts[3].trim()) : LocalDateTime.now())
                    .build();
                submissions.add(submission);
                log.debug("解析CSV行: {} -> {}", line, submission.getTeamName());
            }
        }

        return submissions;
    }

    /**
     * 解析 JSON 格式
     */
    private static List<TeamSubmission> parseJSON(String content) {
        List<TeamSubmission> submissions = new ArrayList<>();

        try {
            JSONObject root = JSON.parseObject(content);

            // 支持两种格式
            JSONArray teams = null;

            if (root.containsKey("teams")) {
                teams = root.getJSONArray("teams");
            } else if (root.containsKey("hackathon")) {
                JSONObject hackathon = root.getJSONObject("hackathon");
                teams = hackathon.getJSONArray("teams");
            }

            if (teams != null) {
                for (int i = 0; i < teams.size(); i++) {
                    JSONObject teamObj = teams.getJSONObject(i);

                    String teamName = teamObj.getString("teamName");
                    if (teamName == null) teamName = teamObj.getString("team_name");

                    String repoUrl = teamObj.getString("repoUrl");
                    if (repoUrl == null) repoUrl = teamObj.getString("repo_url");

                    String email = teamObj.getString("contactEmail");
                    if (email == null) email = teamObj.getString("contact_email");

                    String timeStr = teamObj.getString("submissionTime");
                    if (timeStr == null) timeStr = teamObj.getString("submission_time");

                    List<String> tags = null;
                    if (teamObj.containsKey("tags")) {
                        tags = teamObj.getJSONArray("tags").toJavaList(String.class);
                    }

                    TeamSubmission submission = TeamSubmission.builder()
                        .teamName(teamName)
                        .repoUrl(repoUrl)
                        .contactEmail(email != null ? email : "")
                        .submissionTime(timeStr != null ? parseDateTime(timeStr) : LocalDateTime.now())
                        .tags(tags)
                        .build();

                    submissions.add(submission);
                    log.debug("解析JSON对象: {}", submission.getTeamName());
                }
            }
        } catch (Exception e) {
            log.error("解析JSON失败", e);
            throw new RuntimeException("JSON 解析失败: " + e.getMessage(), e);
        }

        return submissions;
    }

    /**
     * 解析 YAML 格式（简化版，使用JSON解析）
     */
    private static List<TeamSubmission> parseYAML(String content) {
        // 简化实现：将YAML转为JSON格式解析
        // 实际项目中应使用SnakeYAML库
        log.warn("YAML解析暂不完全支持，建议使用JSON或CSV格式");
        return parseJSON(content);
    }

    /**
     * 解析简单文本格式
     * 每行一个URL，或 团队名:URL 格式
     */
    private static List<TeamSubmission> parseSimpleText(String content) {
        List<TeamSubmission> submissions = new ArrayList<>();
        String[] lines = content.split("\\r?\\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String teamName;
            String repoUrl;

            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                teamName = parts[0].trim();
                repoUrl = parts[1].trim();
            } else {
                // 只有URL，从URL提取团队名
                repoUrl = line;
                teamName = extractTeamNameFromUrl(repoUrl);
            }

            TeamSubmission submission = TeamSubmission.builder()
                .teamName(teamName)
                .repoUrl(repoUrl)
                .submissionTime(LocalDateTime.now())
                .build();

            submissions.add(submission);
            log.debug("解析文本行: {} -> {}", line, submission.getTeamName());
        }

        return submissions;
    }

    /**
     * 解析日期时间
     */
    private static LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDateTime.now();
        }

        try {
            return LocalDateTime.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("日期解析失败: {}, 使用当前时间", dateStr);
            return LocalDateTime.now();
        }
    }

    /**
     * 从URL提取团队名
     */
    private static String extractTeamNameFromUrl(String url) {
        // 从URL中提取仓库名作为团队名
        String[] parts = url.split("/");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            return lastPart.replace(".git", "");
        }
        return "Team-" + System.currentTimeMillis();
    }
}

