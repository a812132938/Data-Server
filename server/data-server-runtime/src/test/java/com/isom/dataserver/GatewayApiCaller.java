package com.isom.dataserver;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import java.util.UUID;

/**
 * 网关数据API调用测试 - 直接运行 main 方法即可
 *
 * 使用前请修改下方常量：
 * 1. BASE_URL      - 服务地址
 * 2. API_PATH      - 你定义的API路径
 * 3. APP_KEY       - 创建应用时返回的 appKey
 * 4. APP_SECRET    - 创建应用时返回的 appSecret
 * 5. APP_CODE      - 创建应用时返回的 appCode（用 AppCode 方式时需要）
 */
public class GatewayApiCaller {

    // ========== 请修改以下配置 ==========
    private static final String BASE_URL = "http://localhost:8081";
    private static final String API_PATH = "/gateway/test";   // 改成你的API路径

    // AppKey 签名方式所需
    private static final String APP_KEY = "ak_fmm3lxtb25kjkgbi";               // 改成你的 appKey
    private static final String APP_SECRET = "oc794r2m3wosr83r2rlor0gss190te2o";            // 改成你的 appSecret

    // AppCode 简单方式所需
    private static final String APP_CODE = "ac_3xdji50y5q1psqz80f8zdo8edj9yvgce";              // 改成你的 appCode
    // ===================================

    public static void main(String[] args) {
        System.out.println("===== 方式一：AppKey 签名调用（带参数） =====");
        callWithAppKey("{\"contract_name\": \"测试\"}");

        System.out.println();
        System.out.println("===== 方式二：AppCode 简单调用（带参数） =====");
        callWithAppCode("contract_name=测试");
    }

    /**
     * AppKey + HMAC-SHA256 签名方式调用
     *
     * @param requestBody 请求体JSON字符串，传 null 则不带 body
     */
    public static void callWithAppKey(String requestBody) {
        String url = BASE_URL + API_PATH;
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = UUID.randomUUID().toString().replace("-", "");

        // 签名 = HMAC-SHA256(appKey + timestamp + nonce, appSecret)
        String signStr = APP_KEY + timestamp + nonce;
        String sign = SecureUtil.hmacSha256(APP_SECRET).digestHex(signStr);

        HttpRequest req = HttpRequest.get(url)
                .header("X-App-Key", APP_KEY)
                .header("X-Timestamp", timestamp)
                .header("X-Nonce", nonce)
                .header("X-Sign", sign)
                .header("Content-Type", "application/json");

        if (requestBody != null) {
            req.body(requestBody);
        }

        try (HttpResponse resp = req.execute()) {
            System.out.println("Status: " + resp.getStatus());
            System.out.println("Response: " + resp.body());
        }
    }

    /**
     * AppCode 简单方式调用（无需签名）
     *
     * @param requestBody 请求体JSON字符串，传 null 则不带 body
     */
    public static void callWithAppCode(String queryParams) {
        String url = BASE_URL + API_PATH + "?app_code=" + APP_CODE;
        if (queryParams != null && !queryParams.isEmpty()) {
            url += "&" + queryParams;
        }

        HttpRequest req = HttpRequest.get(url)
                .header("Content-Type", "application/json");

        try (HttpResponse resp = req.execute()) {
            System.out.println("Status: " + resp.getStatus());
            System.out.println("Response: " + resp.body());
        }
    }
}
