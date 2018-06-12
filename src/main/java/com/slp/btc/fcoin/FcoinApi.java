package com.slp.btc.fcoin;

import com.google.gson.Gson;
import com.slp.btc.util.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

public class FcoinApi {
    private static final Logger logger = LoggerFactory.getLogger(FcoinApi.class);
    private final String key;
    private final String secret;
    private Gson gson = new Gson();

    public FcoinApi(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }

    public List<Map<String, String>> accountsBalance() {
        String url = "https://api.fcoin.com/v2/accounts/balance";
        long timeStamp = System.currentTimeMillis();
        String sign = sign("GET", url, timeStamp, null, secret);
        String json = HttpClientUtil.httpGet(url, null, key, sign, timeStamp);
        Map map = gson.fromJson(json, Map.class);
        int status = ((Number) map.get("status")).intValue();
        if (status == 0) {
            return (List<Map<String, String>>) map.get("data");
        } else {
            logger.error("accountsBalance error!!!" + json);
            return null;
        }
    }

    public String orderCreate(String symbol, String side, String type, double price, double amount) {
        String url = "https://api.fcoin.com/v2/orders";

        return null;
    }

    private static String sign(String method, String url, long timeStamp, Map<String, String> params, String secret) {
        try {
            String paramStr = parseParams(params);
            StringBuilder builder = new StringBuilder();
            //设置大写的method，get 或者 post
            builder.append(method.toUpperCase());
            //设置url
            builder.append(url);
            //设置get请求参数
            if ("get".equalsIgnoreCase(method) && StringUtils.isNotBlank(paramStr)) {
                builder.append("?").append(paramStr);
            }
            //设置时间
            builder.append(timeStamp);
            //设置post请求参数
            if ("post".equalsIgnoreCase(method) && StringUtils.isNotBlank(paramStr)) {
                builder.append(paramStr);
            }
            String content = builder.toString();
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] contentByte = encoder.encode(content.getBytes("UTF-8"));
            //HMAC-SHA1 签名
            byte[] bytes = hmacSha1(contentByte, secret.getBytes("UTF-8"));
            return encoder.encodeToString(bytes);
        } catch (Exception e) {
            logger.error("sign error.", e);
        }
        return null;
    }

    private static String parseParams(Map<String, String> params) {
        if (params == null || params.size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        List<String> keyList = new ArrayList<>(params.keySet());
        //对参数按照首字母进行排序
        Collections.sort(keyList);
        for (String key : keyList) {
            builder.append(key).append("=").append(params.get(key)).append("&");
        }
        //删除结尾的&符号
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    private static byte[] hmacSha1(byte[] data, byte[] secret) throws Exception {
        String digestMethod = "HmacSHA1";
        SecretKeySpec signingKey = new SecretKeySpec(secret, digestMethod);
        Mac mac = Mac.getInstance(digestMethod);
        mac.init(signingKey);
        return mac.doFinal(data);
    }
}
