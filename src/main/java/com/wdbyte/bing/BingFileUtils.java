package com.wdbyte.bing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BingFileUtils {

    private static final Path README_PATH = Paths.get("README.md");

    private static final Path BING_PATH = Paths.get("bing-wallpaper.md");

    private static final Path MONTH_PATH = Paths.get("picture/");

    private static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * bing-wallpaper.md 中每行图片记录的格式：2026-09-29 | [图片描述](图片地址)
     */
    private static final Pattern IMAGES_LINE_PATTERN =
        Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}) \\| \\[(.*)\\]\\(([^()]*)\\)$");

    /**
     * 读取 bing-wallpaper.md
     * <p>
     * 返回的列表第一个元素为占位元素（字段均为 null），由调用方填入当日图片。
     *
     * @return
     * @throws IOException
     */
    public static List<Images> readBing() throws IOException {
        List<Images> imgList = new ArrayList<>();
        imgList.add(new Images());
        if (!Files.exists(BING_PATH)) {
            return imgList;
        }
        List<String> allLines = Files.readAllLines(BING_PATH, StandardCharsets.UTF_8);
        for (int i = 1; i < allLines.size(); i++) {
            String line = allLines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            Matcher matcher = IMAGES_LINE_PATTERN.matcher(line);
            if (!matcher.matches()) {
                System.err.println("跳过无法解析的行 " + (i + 1) + "：" + line);
                continue;
            }
            imgList.add(new Images(matcher.group(2), matcher.group(1), matcher.group(3)));
        }
        return imgList;
    }

    /**
     * 写入 bing-wallpaper.md
     *
     * @param imgList
     * @throws IOException
     */
    public static void writeBing(List<Images> imgList) throws IOException {
        StringBuilder content = new StringBuilder("## Bing Wallpaper").append(LINE_SEPARATOR);
        for (Images images : imgList) {
            content.append(images.formatMarkdown()).append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }
        write(BING_PATH, content.toString());
    }

    /**
     * 读取 README.md
     *
     * @return
     * @throws IOException
     */
    public static List<Images> readReadme() throws IOException {
        if (!Files.exists(README_PATH)) {
            Files.createFile(README_PATH);
        }
        List<String> allLines = Files.readAllLines(README_PATH, StandardCharsets.UTF_8);
        List<Images> imgList = new ArrayList<>();
        for (int i = 3; i < allLines.size(); i++) {
            String content = allLines.get(i);
            Arrays.stream(content.split("\\|"))
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    int dateStartIndex = s.indexOf("[", 3) + 1;
                    int urlStartIndex = s.indexOf("(", 4) + 1;
                    String date = s.substring(dateStartIndex, dateStartIndex + 10);
                    String url = s.substring(urlStartIndex, s.length() - 1);
                    return new Images(null, date, url);
                })
                .forEach(imgList::add);
        }
        return imgList;
    }

    /**
     * 写入 README.md
     *
     * @param imgList
     * @throws IOException
     */
    public static void writeReadme(List<Images> imgList) throws IOException {
        List<Images> imagesList = imgList.subList(0, Math.min(30, imgList.size()));
        StringBuilder content = new StringBuilder(buildTable(imagesList, null));
        content.append(LINE_SEPARATOR);
        // 归档
        content.append("### 历史归档：").append(LINE_SEPARATOR);
        List<String> dateList = imgList.stream()
            .map(Images::getDate)
            .map(date -> date.substring(0, 7))
            .distinct()
            .collect(Collectors.toList());
        int i = 0;
        for (String date : dateList) {
            // 使用相对路径，带前导斜杠会被解析成站点根目录导致链接失效
            content.append(String.format("[%s](picture/%s/) | ", date, date));
            i++;
            if (i % 8 == 0) {
                content.append(LINE_SEPARATOR);
            }
        }
        write(README_PATH, content.toString());
    }

    /**
     * 按月份写入图片信息
     *
     * @param imgList
     * @throws IOException
     */
    public static void writeMonthInfo(List<Images> imgList) throws IOException {
        Map<String, List<Images>> monthMap = convertImgListToMonthMap(imgList);
        for (String key : monthMap.keySet()) {
            Path path = MONTH_PATH.resolve(key);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            write(path.resolve("README.md"), buildTable(monthMap.get(key), key));
        }
    }

    /**
     * 转换图片列表为月度 Map
     *
     * @param imagesList
     * @return
     */
    public static Map<String, List<Images>> convertImgListToMonthMap(List<Images> imagesList) {
        Map<String, List<Images>> monthMap = new LinkedHashMap<>();
        for (Images images : imagesList) {
            if (images.getUrl() == null) {
                continue;
            }
            String key = images.getDate().substring(0, 7);
            if (monthMap.containsKey(key)) {
                monthMap.get(key).add(images);
            } else {
                ArrayList<Images> list = new ArrayList<>();
                list.add(images);
                monthMap.put(key, list);
            }
        }
        return monthMap;
    }

    /**
     * 组装图片列表内容
     *
     * @param imagesList
     * @param name      月度归档名称，首页传 null
     * @return
     */
    private static String buildTable(List<Images> imagesList, String name) {
        String title = "## Bing Wallpaper";
        if (name != null) {
            title = "## Bing Wallpaper (" + name + ")";
        }
        StringBuilder content = new StringBuilder(title).append(LINE_SEPARATOR);
        content.append(imagesList.get(0).toLarge()).append(LINE_SEPARATOR);
        content.append("|      |      |      |").append(LINE_SEPARATOR);
        content.append("| :----: | :----: | :----: |").append(LINE_SEPARATOR);
        int i = 1;
        for (Images images : imagesList) {
            content.append("|").append(images);
            if (i % 3 == 0) {
                content.append("|").append(LINE_SEPARATOR);
            }
            i++;
        }
        if (i % 3 != 1) {
            content.append("|");
        }
        return content.toString();
    }

    /**
     * 一次性写入指定文件，统一使用 UTF-8 编码
     *
     * @param path
     * @param content
     * @throws IOException
     */
    private static void write(Path path, String content) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

}
