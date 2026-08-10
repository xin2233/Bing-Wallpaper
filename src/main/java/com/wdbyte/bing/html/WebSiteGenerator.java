package com.wdbyte.bing.html;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.wdbyte.bing.BingFileUtils;
import com.wdbyte.bing.Images;
import com.wdbyte.bing.html.HtmlConstant.Head;
import com.wdbyte.bing.html.HtmlConstant.ImgCard;

public class WebSiteGenerator {

    public static void main(String[] args) throws IOException {
        List<Images> bingImages = BingFileUtils.readBing();
        bingImages = bingImages.stream().filter(img -> img.getUrl() != null).collect(Collectors.toList());
        Map<String, List<Images>> monthMap = BingFileUtils.convertImgListToMonthMap(bingImages);
        HtmlFileUtils.writeMonthsJson(monthMap);
        WebSiteGenerator generator = new WebSiteGenerator();
        generator.htmlGeneratorIndex(bingImages);
        generator.htmlGeneratorMonth(monthMap);
    }

    public void htmlGenerator() throws IOException {
        List<Images> bingImages = BingFileUtils.readBing();
        bingImages = bingImages.stream().filter(img -> img.getUrl() != null).collect(Collectors.toList());
        Map<String, List<Images>> monthMap = BingFileUtils.convertImgListToMonthMap(bingImages);
        HtmlFileUtils.writeMonthsJson(monthMap);
        htmlGeneratorIndex(bingImages);
        htmlGeneratorMonth(monthMap);
    }

    public void htmlGeneratorIndex(List<Images> bingImages) throws IOException {
        String templateFile = HtmlFileUtils.readIndexTemplateFile();
        // 替换头部图片和描述
        String indexHtml = replaceHead(templateFile, bingImages.get(0), null);
        // 替换图片列表
        indexHtml = replaceImgList(indexHtml, bingImages.subList(0, 30));
        // 写到文件
        HtmlFileUtils.writeIndexHtml(indexHtml);
    }

    public void htmlGeneratorMonth(Map<String, List<Images>> monthMap) throws IOException {
        for (String month : monthMap.keySet()) {
            List<Images> bingImages = monthMap.get(month);
            String templateFile = HtmlFileUtils.readIndexTemplateFile();
            // 替换头部图片和描述
            String html = replaceHead(templateFile, bingImages.get(0), month);
            // 替换图片列表
            html = replaceImgList(html, bingImages);
            // 写到文件
            HtmlFileUtils.writeMonthHtml(month, html);
        }
    }

    /**
     * 更新头部大图和描述
     *
     * @param html
     * @param images
     * @param month
     * @return
     */
    public String replaceHead(String html, Images images, String month) {
        html = html.replace(Head.HEAD_IMG_URL, images.getUrl());
        html = html.replace(Head.HEAD_IMG_DESC, images.getDesc());
        if (month != null) {
            html = html.replace(Head.HEAD_TITLE, "Bing Wallpaper(" + month + ")");
        } else {
            html = html.replace(Head.HEAD_TITLE, "Bing Wallpaper");
        }
        return html;
    }

    public String replaceImgList(String html, List<Images> bingImages) {
        StringBuilder imgList = new StringBuilder();
        for (Images bingImage : bingImages) {
            imgList.append(ImgCard.getImgCard(bingImage.getUrl(), bingImage.getDate()));
        }
        return html.replace(ImgCard.VAR_IMG_CARD_LIST, imgList.toString());
    }

}
