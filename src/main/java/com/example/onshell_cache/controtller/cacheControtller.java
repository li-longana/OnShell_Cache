package com.example.onshell_cache.controtller;


import com.example.onshell_cache.db.bean.Key_Value;
import com.example.onshell_cache.service.cache;
import com.onshell.email.Drivers.CacheUtil;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class cacheControtller {
    @Value("${user.password}")
    private String password;
    @Value("${user.ip}")
    private String ip;
    @PostMapping("/setPassword")
    public void setpwd(HttpServletRequest request, @RequestBody String pwd) throws Exception {
        if (jc(request)) {
            password=pwd;
        } else {
            throw new Exception("请求参数不正确");
        }

    }
    @Autowired
    private cache cache;
    @PostMapping("/getall")
    public List<String> getall(HttpServletRequest request) throws Exception {
        if (jc(request)) {
            return cache.get();
        } else {
            throw new Exception("请求参数不正确");
        }

    }
    @PostMapping("/get")
    public String get(HttpServletRequest request,@RequestBody String key) throws Exception {
        if (jc(request)) {
            // 将字符串转换为 JSON 对象
            JSONObject json = new JSONObject(key);
            String key_name = json.getString("key");

            return cache.get(key_name);
        } else {
            throw new Exception("请求参数不正确");
        }

    }
    @PostMapping("/getValue")
    public String getValue(HttpServletRequest request,@RequestBody String key) throws Exception {
        if (jc(request)) {
            // 将字符串转换为 JSON 对象
            JSONObject json = new JSONObject(key);
            String key_name = json.getString("key");

            return cache.getValue(key_name);
        } else {
            throw new Exception("请求参数不正确");
        }

    }
    @PostMapping("/set")
    public boolean set(HttpServletRequest request,@RequestBody Key_Value key_value) throws Exception {
        if (jc(request)) {
            if(key_value.getSeconds()==-1){
                return cache.set(key_value.getKey_name(),key_value.getKey_value());
            }else {
                return cache.set(key_value.getKey_name(),key_value.getKey_value(),key_value.getSeconds());
            }
        } else {
            throw new Exception("请求参数不正确");
        }

    }
    @PostMapping("/del")
    public boolean del(HttpServletRequest request,@RequestBody String key) throws Exception {
        if (jc(request)) {
            // 将字符串转换为 JSON 对象
            JSONObject json = new JSONObject(key);
            String key_name = json.getString("key");
            return cache.del(key_name);
        } else {
            throw new Exception("请求参数不正确");
        }

    }
    //检查请求头
    private boolean jc(HttpServletRequest request){
        // 获取请求头中的密码
        String pwd = request.getHeader("X-Password");
        //获取ip
        String remoteAddr = request.getRemoteAddr();
        if(pwd.equals(password)&&(ip.equals("0.0.0.0") || ip.equals(remoteAddr))){
                return true;
        }else {
            return false;
        }
    }
    //转发body请求
    //防止跨域
    @CrossOrigin(origins = "*")
    @RequestMapping("/forward/**")
    public void forwardRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        // 获取请求路径
        String fullPath = request.getRequestURI();

        // 提取/forward/后面的代码
        String code = "/"+fullPath.substring(fullPath.indexOf("/forward/") + "/forward/".length());
        String prm="";

        // 如果有问号，则忽略问号后面的内容
        int questionMarkIndex = code.indexOf('?');
        if (questionMarkIndex != -1) {
            prm=code.substring(questionMarkIndex);
            code = code.substring(0, questionMarkIndex);
        }
        String rpcHost=CacheUtil.getvalue(code);
        if(rpcHost == null|| rpcHost.equals("")){

        }else {
            String rpcName = CacheUtil.getvalue(code + "name");
            String RpcKey = CacheUtil.getvalue(rpcName + "Key");
            String serverUrl = "http://" + rpcHost + code + prm;

            // 读取请求头
            HttpHeaders headers = new HttpHeaders();
            headers.add(rpcName, RpcKey);
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headers.add(headerName, headerValue);
            }

            // 读取请求体
            byte[] body = IOUtils.toByteArray(request.getInputStream());

            // 创建新的请求实体
            HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);
            System.out.println(fullPath + "============>>" + serverUrl);
            // 发送新的请求并获取响应
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(serverUrl, HttpMethod.POST, entity, byte[].class);

            // 复制响应头
            for (Map.Entry<String, List<String>> entry : responseEntity.getHeaders().entrySet()) {
                for (String value : entry.getValue()) {
                    response.addHeader(entry.getKey(), value);
                }
            }
            byte[] body1 = responseEntity.getBody();
            if (body1 != null) {
                // 复制响应体
                System.out.println("redata：" + new String(body1));
                response.getOutputStream().write(body1);
                System.out.println("getOutputStream");
            } else {
                System.out.println("redata is null");
            }
            return;
        }

    }
    //防止跨域
    @CrossOrigin(origins = "*")
    @RequestMapping("/forward2/**")
    public void forwardRequest2(HttpServletRequest request, HttpServletResponse response) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // Get the request path
        String fullPath = request.getRequestURI();

        // Extract the code after "/forward/"
        String code = "/" + fullPath.substring(fullPath.indexOf("/forward2/") + "/forward2/".length());

        // Extract query parameters if present
        String queryString = request.getQueryString();
        String prm = queryString != null ? "?" + queryString : "";

        String rpcHost = CacheUtil.getvalue(code);
        if(rpcHost == null|| rpcHost.equals("")){

        }else {
            String rpcName = CacheUtil.getvalue(code + "name");
            String rpcKey = CacheUtil.getvalue(rpcName + "Key");
            String serverUrl = "http://" + rpcHost + code + prm;

            // Read request headers
            HttpHeaders headers = new HttpHeaders();
            headers.add(rpcName, rpcKey);
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headers.add(headerName, headerValue);
            }

            // Create an input stream from the request body
            InputStream requestBodyStream = request.getInputStream();

            // Create a request entity with the input stream and headers
            HttpEntity<InputStreamResource> entity = new HttpEntity<>(new InputStreamResource(requestBodyStream), headers);
            System.out.println(fullPath + "============>>" + serverUrl);

            // Send the request and get the response
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(serverUrl, HttpMethod.POST, entity, byte[].class);

            // Copy response headers
            HttpHeaders responseHeaders = responseEntity.getHeaders();
            for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
                for (String value : entry.getValue()) {
                    response.addHeader(entry.getKey(), value);
                }
            }

            byte[] body = responseEntity.getBody();
            if (body != null) {
                // Copy response body
                System.out.println("redata：" + new String(body));
                response.getOutputStream().write(body);
                System.out.println("getOutputStream");

            } else {
                System.out.println("redata is null");
            }
            return;
        }

    }
    //防止跨域
    @CrossOrigin(origins = "*")
    @RequestMapping("/forward3/**")
    public void forwardRequest3(HttpServletRequest request, HttpServletResponse response) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // Get the request path
        String fullPath = request.getRequestURI();

        // Extract the code after "/forward2/"
        String code = "/" + fullPath.substring(fullPath.indexOf("/forward3/") + "/forward3/".length());

        // Extract query parameters if present
        String queryString = request.getQueryString();
        String prm = queryString != null ? "?" + queryString : "";

        String rpcHost = CacheUtil.getvalue(code);
        if(rpcHost == null|| rpcHost.equals("")){

        }else {
            String rpcName = CacheUtil.getvalue(code + "name");
            String rpcKey = CacheUtil.getvalue(rpcName + "Key");
            String serverUrl = "http://" + rpcHost + code + prm;

            // Read request headers
            HttpHeaders headers = new HttpHeaders();
            headers.add(rpcName, rpcKey);
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headers.add(headerName, headerValue);
            }

            // Read and add form data parameters to the MultiValueMap
            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                String[] paramValues = request.getParameterValues(paramName);
                for (String paramValue : paramValues) {
                    formData.add(paramName, paramValue);
                }
            }

            // Read and add file parameters to the MultiValueMap
            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                List<MultipartFile> files = multipartRequest.getFiles("file"); // Replace "file" with the actual parameter name for files
                for (MultipartFile file : files) {
                    formData.add("file", file.getResource());
                }
            }

            // Create a request entity with the form data and headers
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(formData, headers);

            // Send the request and get the response
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(serverUrl, HttpMethod.POST, entity, byte[].class);

            // Copy response headers
            HttpHeaders responseHeaders = responseEntity.getHeaders();
            for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
                for (String value : entry.getValue()) {
                    response.addHeader(entry.getKey(), value);
                }
            }
            byte[] body = responseEntity.getBody();
            if (body != null) {
                // Copy response body
                System.out.println("redata：" + new String(body));
                response.getOutputStream().write(body);
            } else {
                System.out.println("redata is null");
            }
            return;
        }
    }
    //重定向服务
    //防止跨域
    @CrossOrigin(origins = "*")
    @RequestMapping("/forward9/**")
    public String forwardRequest9(HttpServletRequest request, HttpServletResponse response) throws IOException {

            // Get the request path
            String fullPath = request.getRequestURI();

            // Extract the code after "/forward9/"
            String code = "/" + fullPath.substring(fullPath.indexOf("/forward9/") + "/forward9/".length());

            // Extract query parameters if present
            String queryString = request.getQueryString();
            String prm = queryString != null ? "?" + queryString : "";

            String rpcHost = CacheUtil.getvalue(code);
            if (rpcHost == null || rpcHost.equals("")) {
                return null;
            } else {
                String rpcName = CacheUtil.getvalue(code + "name");
                String rpcKey = CacheUtil.getvalue(rpcName + "Key");
                String serverUrl = "http://" + rpcHost + code + prm;
                try {
                // Create a JSON object with the required information
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("rpcname", rpcName);
                jsonObject.put("serverUrl", serverUrl);
                jsonObject.put("rpctoken", rpcKey);

                // Return the JSON string
                return jsonObject.toString();
            }catch(Exception e){
                return e.toString();
            }
        }

    }
}

