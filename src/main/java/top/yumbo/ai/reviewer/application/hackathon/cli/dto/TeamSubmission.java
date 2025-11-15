package top.yumbo.ai.reviewer.application.hackathon.cli.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 团队提交信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamSubmission {
    private String teamName;
    private String repoUrl;
    private String contactEmail;
    private LocalDateTime submissionTime;
    private List<String> tags;
}

