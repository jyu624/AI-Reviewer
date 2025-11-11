AI Reviewer - LLM Preprocessing Tool

This repository contains a lightweight Java CLI tool to preprocess large codebases for LLM-based analysis. It selects core files, extracts concise snippets, and groups them into batches ready to be sent to a large language model.

Quick usage

1. Build (requires Maven):

```bash
mvn -q -DskipTests=true package
```

2. Run ProjectAnalyzer:

```bash
java -cp target/ai-reviewer-1.0.jar top.yumbo.ai.reviewer.ProjectAnalyzer <rootPath> <outDir> <maxCharsPerBatch> <snippetMaxLines> <treeDepth> <includeTests> <topK> <selectionFile>
```

All arguments are optional and have defaults:
- `rootPath` (default: `.`)
- `outDir` (default: `llm_output`)
- `maxCharsPerBatch` (default: `100000`)
- `snippetMaxLines` (default: `200`)
- `treeDepth` (default: `4`)
- `includeTests` (default: `false`)
- `topK` (default: `-1` meaning no top-K)
- `selectionFile` (optional path to a text file containing file paths to analyze, one per line)

Examples

- Default run (analyze current directory):

```bash
java -cp target/ai-reviewer-1.0.jar top.yumbo.ai.reviewer.ProjectAnalyzer
```

- Analyze a repo and only include top 20 prioritized files:

```bash
java -cp target/ai-reviewer-1.0.jar top.yumbo.ai.reviewer.ProjectAnalyzer /path/to/repo out 100000 200 4 false 20
```

- Use a selection file (paths relative to repo root):

```bash
java -cp target\ai-reviewer-1.0.jar top.yumbo.ai.reviewer.ProjectAnalyzer D:\Jetbrains\IdeaProjects\yumbo-music-utils out 100000 200 4 false -1 selection.txt
```

Output

The tool writes outputs to `<outDir>` (default: `llm_output`):
- `project_structure.txt` - project tree (configurable depth)
- `selected_files.txt` - list of selected files with heuristic scores
- `top_k_selected.txt` - optional (when `topK` used)
- `snippets/` - per-file snippet text files
- `batches/` - batched files ready for LLM input
- `batch_index.txt` - metadata about batches (characters, estimated tokens, snippet lists)

Notes

- The tool contains heuristics for token estimation and Java-language-aware snippet splitting (method-level extraction).
- For production use, consider integrating a model-specific tokenization library for accurate token counts and using a proper parser for language-specific splitting.

License: Apache-2.0

