package com.onshell.email.Drivers;


import com.example.onshell_cache.OnshellCacheApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class CacheUtil {
    private static String cachePassword;
    private static String url;
    private static String PASSWORD_HEADER = "X-Password";
    private static String BASE_URL;

    public static void init() {
        ConfigurableApplicationContext ctx = OnshellCacheApplication.getCtx();
        cachePassword = ctx.getEnvironment().getProperty("Cahce.password");
        url = ctx.getEnvironment().getProperty("Cahce.url");
        BASE_URL = "http://" + url;
    }

    private static String buildUrl(String endpoint) {
        return BASE_URL + endpoint;
    }



    private static HttpURLConnection createConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty(PASSWORD_HEADER, cachePassword);
        return connection;
    }

    private static String sendRequest(String url, String method, String requestBody) throws IOException {
        HttpURLConnection connection = createConnection(url);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod(method);

        if (requestBody != null) {
            connection.setDoOutput(true);
            connection.getOutputStream().write(requestBody.getBytes());
            connection.getOutputStream().flush();
            connection.getOutputStream().close();
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        connection.disconnect();
        return response.toString();
    }


    public static void setPassword(String password) throws IOException {
        String url = buildUrl("/setPassword");
        String body = "{\"pwd\":\"" + password + "\"}";

        HttpURLConnection connection = createConnection(url);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.getOutputStream().write(body.getBytes());
        connection.getOutputStream().flush();
        connection.getOutputStream().close();

        connection.getResponseCode();
        connection.disconnect();
    }

    public static String getAll() throws IOException {
        String url = buildUrl("/getall");
        return sendRequest(url, "GET","");
    }

    public static String get(String key) throws IOException {
        String url = buildUrl("/get");
        String body = "{\"key\":\"" + key + "\"}";

        return sendRequest(url, "POST",body);
    }
    public static String getvalue(String key) throws IOException {
        String url = buildUrl("/getValue");
        String body = "{\"key\":\"" + key + "\"}";

        return sendRequest(url, "POST",body);
    }

    public static boolean set(String key, String value, int seconds) throws IOException {
        String url = buildUrl("/set");
        String body = "{\"key_name\":\"" + key + "\",\"key_value\":\"" + value + "\",\"seconds\":" + seconds + "}";

        String response = sendRequest(url, "POST",body);
        return response.equals("true");
    }

    public static boolean del(String key) throws IOException {
        String url = buildUrl("/del");
        String body = "{\"key\":\"" + key + "\"}";

        String response = sendRequest(url, "POST",body);
        return response.equals("true");
    }
    //以下是远程方法调用
    public static String processStringResponse(String url, String... params) {
        // 构建完整的请求URL，将参数拼接到URL中
        StringBuilder fullUrl = new StringBuilder(url);
        if (params.length > 0) {
            fullUrl.append('?');
            for (int i = 0; i < params.length; i++) {
                fullUrl.append(params[i]);
                if (i < params.length - 1) {
                    fullUrl.append('&');
                }
            }
        }

        // 发送HTTP请求，获取返回的字符串结果
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(fullUrl.toString(), String.class);
        return response.getBody();
    }
    public static InputStream processStreamResponse(String url, String... params) {
        // 构建完整的请求URL，将参数拼接到URL中
        StringBuilder fullUrl = new StringBuilder(url);
        if (params.length > 0) {
            fullUrl.append('?');
            for (int i = 0; i < params.length; i++) {
                fullUrl.append(params[i]);
                if (i < params.length - 1) {
                    fullUrl.append('&');
                }
            }
        }

        // 发送HTTP请求，获取返回的字节数组结果
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.getForEntity(fullUrl.toString(), byte[].class);
        byte[] responseBody = response.getBody();

        // 将字节数组包装成输入流并返回
        return new ByteArrayInputStream(responseBody);
    }
    //文件下载
    public static void downloadFile(String url, String path, String serverString, HttpServletResponse response) throws IOException {
        // 创建 URL 对象
        URL requestUrl = new URL(url + "?path=" + path + "&Server=" + serverString);

        // 打开连接
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

        // 设置请求方法
        connection.setRequestMethod("GET");

        // 发起请求
        connection.connect();

        // 获取响应状态码
        int statusCode = connection.getResponseCode();

        if (statusCode == HttpURLConnection.HTTP_OK) {
            // 获取响应文件流
            InputStream inputStream = connection.getInputStream();

            // 获取原接口返回的响应头
            String contentDisposition = connection.getHeaderField("Content-Disposition");
            String contentType = connection.getHeaderField("Content-Type");
            String contentLength = connection.getHeaderField("Content-Length");

            // 设置文件名
            String fileName = new File(path).getName();

            // 设置响应头
            response.setHeader("Content-Disposition", contentDisposition);
            response.setHeader("Content-Type", contentType);
            response.setHeader("Content-Length", contentLength);

            // 获取响应输出流
            ServletOutputStream outputStream = response.getOutputStream();

            try {
                // 将文件流拷贝到响应输出流
                copyInputStreamToOutputStream(inputStream, outputStream);
                outputStream.flush();
            } finally {
                outputStream.close();
            }
        } else {
            // 处理请求失败的情况
            throw new IOException("Failed to download file. Response code: " + statusCode);
        }
    }

    private static void copyInputStreamToOutputStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
    }

//    自动注册

//真正的无脑转发
public static void forwardRequest(String realUrl, InputStream frontendInputStream, OutputStream frontendOutputStream) {
    try {
        // 创建连接
        URL url = new URL(realUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 设置请求方法
        connection.setRequestMethod("POST");

        // 允许输出请求体
        connection.setDoOutput(true);

        // 获取真实接口的输出流
        OutputStream backendOutputStream = connection.getOutputStream();

        // 从前端输入流读取数据，并写入真实接口的输出流
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = frontendInputStream.read(buffer)) != -1) {
            backendOutputStream.write(buffer, 0, bytesRead);
        }

        // 关闭输入流和输出流
        frontendInputStream.close();
        backendOutputStream.close();

        // 获取真实接口的响应码
        int responseCode = connection.getResponseCode();

        // 获取真实接口的响应头
        InputStream backendInputStream;
        if (responseCode >= 200 && responseCode < 300) {
            backendInputStream = connection.getInputStream();
        } else {
            backendInputStream = connection.getErrorStream();
        }

        // 从真实接口的输入流读取数据，并写入前端响应的输出流
        while ((bytesRead = backendInputStream.read(buffer)) != -1) {
            frontendOutputStream.write(buffer, 0, bytesRead);
        }

        // 关闭输入流和输出流
        backendInputStream.close();
        frontendOutputStream.close();

        // 断开连接
        connection.disconnect();
    } catch (IOException e) {
        e.printStackTrace();
    }
}


}
