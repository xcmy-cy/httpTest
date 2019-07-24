package com.example.xcmy.httptest.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * apache httpclient 工具
 * <p>
 * 为了减少请求的创建可以设计成连接池,随用随取.
 *
 * httpget经过简单测试,httppost还未测试
 */
public class HttpClientUtils {
    //默认编码集
    public static final String CHARSET = "UTF-8";

    /**
     * get请求 无参数
     *
     * @param url
     * @return
     */
    public static String httpGet(String url) {
        return httpGet(url, null);
    }

    /**
     * get请求 有参数
     *
     * @param url
     * @param params
     * @return
     */
    public static String httpGet(String url, HashMap<String, String> params) {
        return httpGet(url, null, params);
    }

    /**
     * get请求 有参数 指定请求和响应用一样的字符集
     *
     * @param url
     * @param charset
     * @param params
     * @return
     */
    public static String httpGet(String url, String charset, HashMap<String, String> params) {
        return httpGet(url, charset, null, params);
    }

    /**
     * get请求 有头信息 有参数 指定请求和响应用一样的字符集
     *
     * @param url
     * @param headers
     * @param params
     * @return
     */
    public static String httpGet(String url, String charset, HashMap<String, String> headers, HashMap<String, String> params) {
        return httpGet(url, charset, charset, headers, params, null);
    }

    /**
     * 功能描述:get请求
     *
     * @param url             请求路径
     * @param paramCharset    请求参数字符集 默认UTF-8
     * @param responseCharset 响应字符集 默认UTF-8
     * @param headers         请求头集合
     * @param params          参数集合
     * @param config          超时时间等请求配置对象<code>RequestConfig.custom().setConnectTimeout(1000);</code>
     * @Author: cy
     * @Date: 2019-07-24 14:32
     **/
    public static String httpGet(String url, String paramCharset, String responseCharset, HashMap<String, String> headers, HashMap<String, String> params, RequestConfig config) {

        String returnString = null;
        //创建HttpClient对象
        HttpClient httpClient = HttpClients.createDefault();

        try {
            //添加参数
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> pairs = new ArrayList<>(params.size());
                params.forEach((k, v) -> pairs.add(new BasicNameValuePair(k, v)));

                url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, paramCharset == null ? CHARSET : paramCharset));
            }

            //创建HttpGet链接
            HttpGet httpGet = new HttpGet(url);

            //设置请求头
            headersInit(httpGet, headers);

            //构建超时等配置信息
            RequestConfigInit(httpGet, config);

            //发送请求
            HttpResponse response = httpClient.execute(httpGet);

            //解析响应
            returnString = responsenalyze(response, responseCharset == null ? CHARSET : responseCharset);

        } catch (Exception e) {
            //根据需求更改异常操作
            e.printStackTrace();
        }

        return returnString;
    }

    /**
     * post请求 无参数
     *
     * @param url
     * @return
     */
    public static String httpPost(String url) {
        //无法区分所以传了一个空字符串
        return httpPost(url, "");
    }

    /**
     * post请求 json参数
     *
     * @param url
     * @param param
     * @return
     */
    public static String httpPost(String url, String param) {
        return httpPost(url, null, param);
    }

    /**
     * post请求 json参数 字符集
     *
     * @param url
     * @param charset
     * @param param
     * @return
     */
    public static String httpPost(String url, String charset, String param) {
        return httpPost(url, charset, null, param);
    }

    /**
     * post请求 json参数 字符集 请求头
     *
     * @param url
     * @param charset
     * @param headers
     * @param param
     * @return
     */
    public static String httpPost(String url, String charset, HashMap<String, String> headers, String param) {
        return httpPost(url, charset, charset, headers, param, null);
    }


    /**
     * post请求 json参数
     *
     * @param url             路径
     * @param paramCharset    请求参数字符集
     * @param responseCharset 响应字符集
     * @param headers         请求头
     * @param param           请求参数json字符串
     * @param config          请求配置信息
     * @return
     */
    public static String httpPost(String url, String paramCharset, String responseCharset, HashMap<String, String> headers, String param, RequestConfig config) {

        String returnString = null;
        //创建HttpClient对象
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        //参数转换 解决中文乱码问题
        if (param != null && param.trim().length() > 0) {
            paramCharset = paramCharset == null ? CHARSET : paramCharset;
            StringEntity entity = new StringEntity(param, paramCharset);
            //简单区分是json字符串还是xml字符串
            if (param.startsWith("{") && param.endsWith("}")) {
                entity.setContentType("application/json");
            }
            if (param.startsWith("<") && param.endsWith(">")) {
                entity.setContentType("application/xml");
            }
            entity.setContentEncoding(paramCharset);
            httpPost.setEntity(entity);
        }

        //设置请求头
        headersInit(httpPost, headers);

        //构建超时等配置信息
        RequestConfigInit(httpPost, config);

        try {
            //发送post请求
            HttpResponse response = httpClient.execute(httpPost);

            //解析响应
            returnString = responsenalyze(response, responseCharset == null ? CHARSET : responseCharset);

        } catch (Exception e) {
            //根据需求更改异常操作
            e.printStackTrace();
        }

        return returnString;
    }

    /**
     * post请求 map参数
     *
     * @param url
     * @param params
     * @return
     */
    public static String httpPost(String url, HashMap<String, String> params) {
        return httpPost(url, params);
    }

    /**
     * post请求 map参数 字符集
     *
     * @param url
     * @param charset
     * @param params
     * @return
     */
    public static String httpPost(String url, String charset, HashMap<String, String> params) {
        return httpPost(url, charset, null, params);
    }

    /**
     * post请求 map参数 字符集 请求头
     *
     * @param url
     * @param charset
     * @param headers
     * @param params
     * @return
     */
    public static String httpPost(String url, String charset, HashMap<String, String> headers, HashMap<String, String> params) {
        return httpPost(url, charset, charset, headers, params, null);
    }

    /**
     * post请求 json参数
     *
     * @param url             路径
     * @param paramCharset    请求参数字符集
     * @param responseCharset 响应字符集
     * @param headers         请求头
     * @param params          请求参数Map集合
     * @param config          请求配置信息
     * @return
     */
    public static String httpPost(String url, String paramCharset, String responseCharset, HashMap<String, String> headers, HashMap<String, String> params, RequestConfig config) {

        String returnString = null;
        //创建HttpClient对象
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        //设置请求头
        headersInit(httpPost, headers);

        //构建超时等配置信息
        RequestConfigInit(httpPost, config);

        try {
            //添加参数
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> pairs = new ArrayList<>(params.size());
                params.forEach((k, v) -> pairs.add(new BasicNameValuePair(k, v)));
                httpPost.setEntity(new UrlEncodedFormEntity(pairs, paramCharset == null ? CHARSET : paramCharset));
            }

            //发送post请求
            HttpResponse response = httpClient.execute(httpPost);

            //解析响应
            returnString = responsenalyze(response, responseCharset == null ? CHARSET : responseCharset);

        } catch (Exception e) {
            //根据需求更改异常操作
            e.printStackTrace();
        }

        return returnString;
    }

    /**
     * 添加请求头
     *
     * @param base
     * @param headers
     * @return
     */
    private static void headersInit(HttpRequestBase base, HashMap<String, String> headers) {

        if (headers != null && !headers.isEmpty())
            headers.forEach((k, v) -> base.addHeader(k, v));
    }

    /**
     * 设置请求配置 如:超时时间等
     *
     * @param base
     * @param config
     */
    private static void RequestConfigInit(HttpRequestBase base, RequestConfig config) {
        if (config != null)
            base.setConfig(config);
    }

    /**
     * 解析响应
     *
     * @param response
     * @param responseCharset
     * @return
     * @throws IOException
     */
    private static String responsenalyze(HttpResponse response, String responseCharset) throws IOException {

        // 判断响应状态
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

            //在指定字符集后解析响应时依然会遇到中文乱码的情况.需要考虑指定字符集和服务端字符集是否一致,可以通过
            //System.out.println(response.getEntity());
            //查看响应使用的字符集.
            //解决方案一般为将响应解析出来的字符串通过new String()进行解码和编码.也可以在服务端的MVC的Mapping标签中添加produces = "text/plain;charset=utf-8"属性

            //读取服务器返回过来的json字符串数据
            return EntityUtils.toString(response.getEntity(), responseCharset);
        }
        return null;
    }
}
