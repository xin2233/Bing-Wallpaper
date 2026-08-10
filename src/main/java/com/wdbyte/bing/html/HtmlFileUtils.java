package com.wdbyte.bing.html;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.wdbyte.bing.Images;

public class HtmlFileUtils {

    private static Path BING_HTML_ROOT = Paths.get("docs/");
    private static Path BING_HTML_INDEX_TEMPLATE = Paths.get("docs/bing-template.html");

    public static String readIndexTemplateFile() throws IOException {
        byte[] bytes = Files.readAllBytes(BING_HTML_INDEX_TEMPLATE);
        return new String(bytes);
    }

    public static void writeIndexHtml(String html) throws IOException {
        Path path = BING_HTML_ROOT.resolve("index.html");
       write(path,html);
    }
    private static void write(Path path,String html) throws IOException {
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        Files.write(path, html.getBytes(StandardCharsets.UTF_8));
    }

    public static void writeMonthHtml(String month, String html) throws IOException {
        Path path = BING_HTML_ROOT.resolve( month + ".html");
        write(path, html);
    }

    /**
     * 写入月份导航数据 months.json
     * 结构: {"months":["2026-08","2026-01",...],"latest":"2026-08"}
     * 页面导航由 JS 动态读取该文件渲染，避免新增月份时全量重写历史页面
     *
     * @param monthMap
     * @throws IOException
     */
    public static void writeMonthsJson(Map<String, List<Images>> monthMap) throws IOException {
        StringBuilder json = new StringBuilder("{\"months\":[");
        boolean first = true;
        String latest = null;
        for (String month : monthMap.keySet()) {
            if (latest == null) {
                latest = month;
            }
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(month).append("\"");
            first = false;
        }
        json.append("]");
        if (latest != null) {
            json.append(",\"latest\":\"").append(latest).append("\"");
        }
        json.append("}");
        Path path = BING_HTML_ROOT.resolve("months.json");
        write(path, json.toString());
    }
}
