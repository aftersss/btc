package cn.com.btc.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

public class Sign {
    private static final Logger logger = LoggerFactory.getLogger(Sign.class);

    /**
     * 签名
     * @param method get 或者 post
     * @param url 访问链接
     * @param timeStamp 时间戳
     * @param params 参数集合
     * @param secret 密钥
     * @return 签名
     */
    public static String sign(String method, String url, long timeStamp, Map<String, String> params, String secret) {
        try {
            String paramStr = parseParams(params);
            StringBuilder builder = new StringBuilder();
            //设置大写的method，get 或者 post
            builder.append(method.toUpperCase()).append(" ");
            //设置url
            builder.append(url);
            //设置get请求参数
            if("get".equalsIgnoreCase(method) && StringUtils.isNotBlank(paramStr)){
                builder.append("?").append(paramStr);
            }
            //设置时间
            builder.append(timeStamp);
            //设置post请求参数
            if("post".equalsIgnoreCase(method) && StringUtils.isNotBlank(paramStr)) {
                builder.append(paramStr);
            }
            String content = builder.toString();
            logger.info(content);
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

    /**
     * 解析请求参数
     * @param params 参数集合
     * @return url参数列表
     */
    private static String parseParams(Map<String, String> params){
        if(params == null || params.size() == 0){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        List<String> keyList = new ArrayList<>(params.keySet());
        //对参数按照首字母进行排序
        Collections.sort(keyList);
        for(String key : keyList) {
            builder.append(key).append("=").append(params.get(key)).append("&");
        }
        //删除结尾的&符号
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    /**
     * HMAC-SHA1 签名
     * @param data base64数据
     * @param secret 密钥
     * @return 二进制签名数据
     * @throws Exception 异常
     */
    private static byte[] hmacSha1(byte[] data, byte[] secret) throws Exception{
        String digestMethod = "HmacSHA1";
        SecretKeySpec signingKey = new SecretKeySpec(secret, digestMethod);
        Mac mac = Mac.getInstance(digestMethod);
        mac.init(signingKey);
        return mac.doFinal(data);
    }

}
