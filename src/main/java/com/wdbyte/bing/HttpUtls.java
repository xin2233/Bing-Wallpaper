package com.wdbyte.bing;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpUtls {

    private static final int CONNECT_TIMEOUT = 10 * 1000;

    private static final int READ_TIMEOUT = 30 * 1000;

    /**
     * 获取 HTTP 连接
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static HttpURLConnection getHttpUrlConnection(String url) throws IOException {
        // new URL(String) 自 JDK 20 起已过时，官方推荐使用 URI 转换
        URL httpUrl = URI.create(url).toURL();
        HttpURLConnection httpConnection = (HttpURLConnection)httpUrl.openConnection();
        httpConnection.setConnectTimeout(CONNECT_TIMEOUT);
        httpConnection.setReadTimeout(READ_TIMEOUT);
        httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");
        return httpConnection;
    }

    /**
     * 请求指定 URL 返回内容
     *
     * @param url
     * @return
     * @throws IOException 请求失败时抛出，避免返回空内容让调用方误判
     */
    public static String getHttpContent(String url) throws IOException {
        HttpURLConnection httpUrlConnection = getHttpUrlConnection(url);
        try (InputStream input = httpUrlConnection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(input)) {
            // 先读取完整字节再一次性解码，避免多字节字符被缓冲区边界截断
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            httpUrlConnection.disconnect();
        }
    }

}
