package cn.com.btc.config;

public enum UrlConfig {
    //查询服务器时间
    SERVER_TIME("https://api.fcoin.com/v2/public/server-time", "GET"),

    //查询可用币种
    CURRENCIES("https://api.fcoin.com/v2/public/currencies", "GET"),

    //查询可用交易对
    SYMBOLS("https://api.fcoin.com/v2/public/symbols", "GET"),

    //获取 ticker 数据，使用时需要用具体交易对名称替换$symbol
    MARKET_TICKER("https://api.fcoin.com/v2/market/ticker/$symbol", "GET"),

    //获取最新的深度明细
    //使用时需要用具体交易对名称替换$symbol，具体查看的种类替换$level
    //$level 取值：L20 20 档行情深度  L100 100 档行情深度
    MARKET_DEPTH("https://api.fcoin.com/v2/market/depth/$level/$symbol", "GET"),

    //获取最新的成交明细 使用时需要用具体交易对名称替换$symbol
    MARKET_TRADES("https://api.fcoin.com/v2/market/trades/$symbol", "GET"),

    //查询账户资产
    ACCOUNTS_BALANCE("https://api.fcoin.com/v2/accounts/balance", "GET"),

    //创建新的订单
    ORDERS("https://api.fcoin.com/v2/orders", "POST"),

    //查询订单列表，默认返回20条订单
    QUERY_ORDER_LIST("https://api.fcoin.com/v2/orders", "GET"),

    //获取指定订单
    GET_ORDER("https://api.fcoin.com/v2/orders/{order_id}", "GET"),

    //申请撤销订单
    ORDER_SUBMIT_CANCEL("https://api.fcoin.com/v2/orders/{order_id}/submit-cancel", "POST"),

    //查询指定订单的成交记录
    ORDER_MATCH_RESULTS("https://api.fcoin.com/v2/orders/{order_id}/match-results", "GET")


    ;

    private UrlConfig(String url, String method){
        this.url = url;
        this.method = method;
    }

    private String url;
    private String method;

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }
}
