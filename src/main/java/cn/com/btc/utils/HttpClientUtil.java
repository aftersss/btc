package cn.com.btc.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class HttpClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private static RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(10000)
            .setConnectionRequestTimeout(10000)
            .setSocketTimeout(120000)
            .build();

    public static void main(String[] args) {
        //String httpUrl = "http://172.16.28.186:9666/catdata/metricForSum";
        //Map<String, String> map = new HashMap<>();
        //map.put("metricKey", "consumerOrderFeature-totalDelayCount");
        //map.put("value", "10");
        //System.out.println(httpGet(httpUrl, map));
    }

    public static String httpGet(String httpUrl, Map<String, String> paramMap,
                                 String accessKey, String sign, long timestamp) {
        CloseableHttpClient httpClient = null;
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
            httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(httpUrl);
            httpGet.setConfig(requestConfig);
            //设置http请求头
            addHttpHeader(httpGet, accessKey, sign, timestamp);
            //执行请求
            response = httpClient.execute(httpGet);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            logger.error("http get error.", e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }

    public static String httpPost(String httpUrl, String content,
                                  String accessKey, String sign, long timestamp) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            //创建httpPost
            HttpPost httpPost = new HttpPost(httpUrl);
            if (StringUtils.isNotBlank(content)) {
                StringEntity stringEntity = new StringEntity(content, "UTF-8");
                stringEntity.setContentType("application/json");
                httpPost.setEntity(stringEntity);
            }
            httpClient = HttpClients.createDefault();
            httpPost.setConfig(requestConfig);

            //设置http请求头
            addHttpHeader(httpPost, accessKey, sign, timestamp);

            //执行请求
            response = httpClient.execute(httpPost);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            logger.error("http post error.", e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
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
