package cn.com.btc.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class HttpClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private static RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(10000)
            .setConnectionRequestTimeout(10000)
            .setSocketTimeout(120000)
            .build();

    private static final CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(10000).setSocketTimeout(120000).setConnectionRequestTimeout(10).build())
            .setMaxConnPerRoute(100)
            .setMaxConnTotal(5000)
//            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36")//userAgent仿真，以防第三方设置的WAF判断了UA从而调用失败.
            .disableAutomaticRetries()//禁止重试
            .disableCookieManagement()
            .useSystemProperties()//for proxy
            .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy(){
                @Override
                public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                    long time = super.getKeepAliveDuration(response, context);
                    if(time == -1){
                        time = 30000;//链接最多空闲30秒
                    }
                    return time;
                }
            }).build();

    public static void main(String[] args) {
        //String httpUrl = "http://172.16.28.186:9666/catdata/metricForSum";
        //Map<String, String> map = new HashMap<>();
        //map.put("metricKey", "consumerOrderFeature-totalDelayCount");
        //map.put("value", "10");
        //System.out.println(httpGet(httpUrl, map));
    }

    public static String httpGet(String httpUrl, Map<String, String> paramMap,
                                 String accessKey, String sign, long timestamp) {
        CloseableHttpResponse response = null;
        try {
            if (paramMap != null && paramMap.size() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(httpUrl).append("?");
                for (String key : paramMap.keySet()) {
                    String value = paramMap.get(key);
                    sb.append(key).append("=").append(value).append("&");
                }
                sb.deleteCharAt(sb.length() - 1);
                httpUrl = sb.toString();
            }
            HttpGet httpGet = new HttpGet(httpUrl);
//            httpGet.setConfig(requestConfig);
            //设置http请求头
            addHttpHeader(httpGet, accessKey, sign, timestamp);
            //执行请求
            response = httpClient.execute(httpGet);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            logger.error("http get error.", e);
        }
        return null;
    }

    public static String httpPost(String httpUrl, String content,
                                  String accessKey, String sign, long timestamp) {
        CloseableHttpResponse response = null;
        try {
            //创建httpPost
            HttpPost httpPost = new HttpPost(httpUrl);
            if (StringUtils.isNotBlank(content)) {
                StringEntity stringEntity = new StringEntity(content, "UTF-8");
                stringEntity.setContentType("application/json");
                httpPost.setEntity(stringEntity);
            }
//            httpPost.setConfig(requestConfig);

            //设置http请求头
            addHttpHeader(httpPost, accessKey, sign, timestamp);

            //执行请求
            response = httpClient.execute(httpPost);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            logger.error("http post error.", e);
        }
        return null;
    }

    private static void addHttpHeader(HttpGet httpGet, String accessKey, String sign, long timestamp) {
        if (StringUtils.isNotBlank(accessKey)) {
            httpGet.addHeader("FC-ACCESS-KEY", accessKey);
        }
        if (StringUtils.isNotBlank(sign)) {
            httpGet.addHeader("FC-ACCESS-SIGNATURE", sign);
        }
        httpGet.addHeader("FC-ACCESS-TIMESTAMP", timestamp + "");
    }

    private static void addHttpHeader(HttpPost httpPost, String accessKey, String sign, long timestamp) {
        if (StringUtils.isNotBlank(accessKey)) {
            httpPost.addHeader("FC-ACCESS-KEY", accessKey);
        }
        if (StringUtils.isNotBlank(sign)) {
            httpPost.addHeader("FC-ACCESS-SIGNATURE", sign);
        }
        httpPost.addHeader("FC-ACCESS-TIMESTAMP", timestamp + "");
    }
}
