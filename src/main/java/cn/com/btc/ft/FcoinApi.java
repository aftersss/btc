package cn.com.btc.ft;

import cn.com.btc.config.UrlConfig;
import cn.com.btc.utils.HttpClientUtil;
import cn.com.btc.utils.Sign;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FcoinApi {
    private static final Logger logger = LoggerFactory.getLogger(FcoinApi.class);
    private final String key;
    private final String secret;
    private Gson gson = new Gson();

    public FcoinApi(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }

    private String httpExecute(String url, String method, Map<String, String> paramMap,
                               String accessKey, String sign, long timeStamp) {
        String result = "";
        if ("get".equalsIgnoreCase(method)) {
            result = HttpClientUtil.httpGet(url, paramMap, accessKey, sign, timeStamp);
        } else if ("post".equalsIgnoreCase(method)) {
            result = HttpClientUtil.httpPost(url, gson.toJson(paramMap), accessKey, sign, timeStamp);
        }
        return result;
    }

    /**
     * 查询服务器时间
     *
     * @return 服务器时间
     */
    public String getServerTime() {
        String url = UrlConfig.SERVER_TIME.getUrl();
        return httpExecute(url, UrlConfig.SERVER_TIME.getMethod(), null, null, null, System.currentTimeMillis());
    }

    /**
     * 查询可用币种
     *
     * @return json
     */
    public String currencies() {
        String url = UrlConfig.CURRENCIES.getUrl();
        return httpExecute(url, UrlConfig.CURRENCIES.getMethod(), null, null, null, System.currentTimeMillis());
    }

    /**
     * 查询可用交易对
     *
     * @return json
     */
    public String symbols() {
        String url = UrlConfig.SYMBOLS.getUrl();
        return httpExecute(url, UrlConfig.SYMBOLS.getMethod(), null, null, null, System.currentTimeMillis());
    }

    /**
     * 查询账户资产
     *
     * @return json
     */
    public Object accountsBalance() {
        String url = UrlConfig.ACCOUNTS_BALANCE.getUrl();
        long timeStamp = System.currentTimeMillis();
        String sign = Sign.sign(UrlConfig.ACCOUNTS_BALANCE.getMethod(), url, timeStamp, null, secret);
        if (StringUtils.isBlank(sign)) {
            return null;
        }
        String json = httpExecute(url, UrlConfig.ACCOUNTS_BALANCE.getMethod(), null, key, sign, timeStamp);
        Map map = gson.fromJson(json, Map.class);
        int status = ((Number) map.get("status")).intValue();
        if (status == 0) {
            return map.get("data");
        } else {
            logger.error("accountsBalance error!!!" + json);
            return null;
        }
    }

    /**
     * 获取 ticker 数据
     *
     * @param symbol 交易对名称
     * @return json
     */
    public String marketTicker(String symbol) {
        if (StringUtils.isBlank(symbol)) {
            return "";
        }
        long timeStamp = System.currentTimeMillis();
        String url = UrlConfig.MARKET_TICKER.getUrl()
                .replace("$symbol", symbol);
        String sign = Sign.sign(UrlConfig.MARKET_TICKER.getMethod(), url, timeStamp, null, secret);
        if (StringUtils.isBlank(sign)) {
            return "";
        }
        return httpExecute(url, UrlConfig.MARKET_TICKER.getMethod(), null, key, sign, timeStamp);
    }

    /**
     * 获取最新的深度明细
     *
     * @param level  L20 20 档行情深度  L100 100 档行情深度
     * @param symbol 交易对名称
     * @return json
     */
    public Object marketDepth(String level, String symbol) {
        if (StringUtils.isBlank(level) || StringUtils.isBlank(symbol)) {
            return null;
        }
        long timeStamp = System.currentTimeMillis();
        String url = UrlConfig.MARKET_DEPTH.getUrl()
                .replace("$level", level)
                .replace("$symbol", symbol);
        String sign = Sign.sign(UrlConfig.MARKET_DEPTH.getMethod(), url, timeStamp, null, secret);
        if (StringUtils.isBlank(sign)) {
            return null;
        }
        String json = httpExecute(url, UrlConfig.MARKET_DEPTH.getMethod(), null, key, sign, timeStamp);
        Map map = gson.fromJson(json, Map.class);
        int status = ((Number) map.get("status")).intValue();
        if (status == 0) {
            return map.get("data");
        } else {
            logger.error("marketDepth error!!!" + json);
            return null;
        }
    }

    /**
     * 获取最新的成交明细，默认取5条记录
     *
     * @param symbol 交易对名称
     * @return json
     */
    public String marketTrades(String symbol, String limit) {
        if (StringUtils.isBlank(symbol)) {
            return "";
        }
        if (StringUtils.isBlank(limit)) {
            limit = "5";
        }
        String url = UrlConfig.MARKET_TRADES.getUrl()
                .replace("$symbol", symbol);
        long timeStamp = System.currentTimeMillis();
        Map<String, String> map = new HashMap<>();
        map.put("limit", limit);
        String sign = Sign.sign(UrlConfig.MARKET_TRADES.getMethod(), url, timeStamp, map, secret);
        if (StringUtils.isBlank(sign)) {
            return "";
        }
        return httpExecute(url, UrlConfig.MARKET_TRADES.getMethod(), map, key, sign, timeStamp);
    }

    /**
     * 创建新订单
     *
     * @param symbol 交易对名称
     * @param side   交易方向（buy, sell）
     * @param type   订单类型（limit 限价交易，market 市价交易）
     * @param price  价格
     * @param amount 数量
     * @return json
     */
    public Object orders(String symbol, String side, String type, String price, String amount) {
        if (StringUtils.isBlank(symbol) || StringUtils.isBlank(side)
                || StringUtils.isBlank(type) || StringUtils.isBlank(price)
                || StringUtils.isBlank(amount)) {
            return null;
        }
        String url = UrlConfig.ORDERS.getUrl();
        long timeStamp = System.currentTimeMillis();
        Map<String, String> map = new HashMap<>();
        map.put("symbol", symbol);
        map.put("side", side);
        map.put("type", type);
        map.put("price", price);
        map.put("amount", amount);
        String sign = Sign.sign(UrlConfig.ORDERS.getMethod(), url, timeStamp, map, secret);
        if (StringUtils.isBlank(sign)) {
            return null;
        }
        String json = httpExecute(url, UrlConfig.ORDERS.getMethod(), map, key, sign, timeStamp);
        Map map1 = gson.fromJson(json, Map.class);
        int status = ((Number) map1.get("status")).intValue();
        if (status == 0) {
            return map1.get("data");
        } else {
            logger.error("orders error!!!" + json);
            return null;
        }
    }

    /**
     * 查询订单列表,默认返回10条订单
     *
     * @param symbol 交易对名称
     * @param states 订单状态(submitted 已提交,partial_filled 部分成交,partial_canceled	部分成交已撤销,filled 完全成交,canceled 已撤销,pending_cancel 撤销已提交)
     * @param before 查询某个页码之前的订单
     * @param after  查询某个页码之后的订单
     * @param limit  每页的订单数量,不填默认返回10条
     * @return json
     */
    public Object queryOrderList(String symbol, String states, String before, String after, String limit) {
        if (StringUtils.isBlank(symbol) || StringUtils.isBlank(states)) {
            return "";
        }
        if (StringUtils.isBlank(limit)) {
            limit = "10";
        }
        String url = UrlConfig.QUERY_ORDER_LIST.getUrl();
        long timeStamp = System.currentTimeMillis();
        Map<String, String> map = new HashMap<>();
        map.put("symbol", symbol);
        map.put("states", states);
        map.put("limit", limit);
        if (StringUtils.isNotBlank(before)) {
            map.put("before", before);
        }
        if (StringUtils.isNotBlank(after)) {
            map.put("after", after);
        }
        String sign = Sign.sign(UrlConfig.QUERY_ORDER_LIST.getMethod(), url, timeStamp, map, secret);
        if (StringUtils.isBlank(sign)) {
            return "";
        }
        String json = httpExecute(url, UrlConfig.QUERY_ORDER_LIST.getMethod(), map, key, sign, timeStamp);
        Map map1 = gson.fromJson(json, Map.class);
        int status = ((Number) map1.get("status")).intValue();
        if (status == 0) {
            return map1.get("data");
        } else {
            logger.error("orders error!!!" + json);
            return null;
        }
    }

    /**
     * 获取指定订单
     *
     * @param orderId 订单id
     * @return json
     */
    public Object getOrder(String orderId) {
        if (StringUtils.isBlank(orderId)) {
            return "";
        }
        String url = UrlConfig.GET_ORDER.getUrl()
                .replace("{order_id}", orderId);
        long timeStamp = System.currentTimeMillis();
        String sign = Sign.sign(UrlConfig.GET_ORDER.getMethod(), url, timeStamp, null, secret);
        if (StringUtils.isBlank(sign)) {
            return "";
        }
        String json = httpExecute(url, UrlConfig.GET_ORDER.getMethod(), null, key, sign, timeStamp);
        Map map1 = gson.fromJson(json, Map.class);
        int status = ((Number) map1.get("status")).intValue();
        if (status == 0) {
            return map1.get("data");
        } else {
            logger.error("orders error!!!" + json);
            return null;
        }
    }

    /**
     * 申请撤销订单
     *
     * @param orderId 订单id
     * @return json
     */
    public boolean orderSubmitCancel(String orderId) {
        if (StringUtils.isBlank(orderId)) {
            return false;
        }
        String url = UrlConfig.ORDER_SUBMIT_CANCEL.getUrl()
                .replace("{order_id}", orderId);
        long timeStamp = System.currentTimeMillis();
        String sign = Sign.sign(UrlConfig.ORDER_SUBMIT_CANCEL.getMethod(), url, timeStamp, null, secret);
        if (StringUtils.isBlank(sign)) {
            return false;
        }
        String json = httpExecute(url, UrlConfig.ORDER_SUBMIT_CANCEL.getMethod(), null, key, sign, timeStamp);
        Map map1 = gson.fromJson(json, Map.class);
        int status = ((Number) map1.get("status")).intValue();
        if (status == 0) {
            return true;
        } else {
            logger.error("orders error!!!" + json);
            return false;
        }
    }

    /**
     * 查询指定订单的成交记录
     *
     * @param orderId 订单id
     * @return json
     */
    public String orderMatchResults(String orderId) {
        if (StringUtils.isBlank(orderId)) {
            return "";
        }
        String url = UrlConfig.ORDER_MATCH_RESULTS.getUrl()
                .replace("{order_id}", orderId);
        long timeStamp = System.currentTimeMillis();
        String sign = Sign.sign(UrlConfig.ORDER_MATCH_RESULTS.getMethod(), url, timeStamp, null, secret);
        if (StringUtils.isBlank(sign)) {
            return "";
        }
        return httpExecute(url, UrlConfig.ORDER_MATCH_RESULTS.getMethod(), null, key, sign, timeStamp);
    }
}
