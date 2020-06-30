package com.bigbigwork.services;


import com.bigbigwork.util.HttpClientTools;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PinterestLoginService extends BaseService {

    private static final Pattern HTTP_STATUS_PAT = Pattern.compile("\"http_status\":\\s*([0-9]+)");
    private static final String LOG_IN_URL = "https://www.pinterest.com/resource/UserSessionResource/create/";

    Map<String, String> headers = getHead();

    public PinterestLoginService(Consumer<String> print) {
        super(print);
        serviceName = "pinterest 用户登录：";
    }

    public void process() {
        int times = 0;
        for (int i = 0; i < getRequests() && run.get(); i++) {
            try {
                String userName = PinterestRegisterService.createUserName(5,10) + "@outlook.com";
                Map<String, String> params = getLogInParams(userName);

                String content = HttpClientTools.doPostMap(LOG_IN_URL, params, headers);
                if(null != content && !content.isEmpty()){
                    Matcher matcher = HTTP_STATUS_PAT.matcher(content);
                    if(run.get()){
                        if(matcher.find()){
                            String status = matcher.group(1);
                            message(String.format("用户名：%s  登录状态:%s", userName, status));
                            if(status.equals("429")){
                                if(++times >= 5) break;
                            }else{
                                times = 0;
                            }
                        }else{
                            message("接口获取状态失败");
                            onError();
                        }
                    }

                }else{
                    message("接口获取内容失败");
                    onError();
                }
            }catch (Exception e){
                LOG.error("error:",e);
            }finally {
                afterRequest();
            }


        }
    }


    private static Map<String, String> getLogInParams(String userName) {
        Map<String, String> params = new HashMap<>();
        params.put("source_url","/");
        params.put("data", String.format("{\"options\":{\"username_or_email\":\"%s\",\"password\":\"1231ddfee-- **1safsd\",\"app_type_from_client\":5,\"visited_pages_before_login\":null,\"tokenV3\":\"default\"},\"context\":{}}", userName));
        return params;
    }


    private static Map<String, String> getHead() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-pinterest-appstate","active");
        headers.put("x-app-version","35d3566");
        headers.put("x-requested-with","XMLHttpRequest");
        headers.put("user-agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.116 Safari/537.36");
        headers.put("content-type","application/x-www-form-urlencoded");
        headers.put("x-pinterest-experimenthash","f5ed47c46929fb64d93c31b772f194fbf884c01a3c7ff5444a68390db7bea177980411a0e6c929d5760d43b783db9355b01b93e766a8752904e95dde3dafd84d");
        headers.put("x-csrftoken","fdb48c9784a30c1478142ace8fa936c9");
        headers.put("origin","https://www.pinterest.com");
        headers.put("sec-fetch-site","same-origin");
        headers.put("sec-fetch-mode","cors");
        headers.put("referer","https://www.pinterest.com/");
        headers.put("cookie","_routing_id=\"438d48e0-3f4f-496e-a7c2-1171e2fb6d7e\"; csrftoken=fdb48c9784a30c1478142ace8fa936c9; sessionFunnelEventLogged=1; G_ENABLED_IDPS=google; bei=false; cm_sub=none; logged_out=True; _auth=0; fba=True; _pinterest_sess=TWc9PSZIT3lHOThNQmF1QlJmUEd3M0FtZjF4YkJMUFJHUjZYSVd0NHhuQ3Y1dWdLdDZhRTBpYzBaSFZZT2xoTVREWnZhNW1DMHFNcmVYWWp0d3NObnovSmFLVmRpNy9VRmlUcStEUHFPejhmNENqTkx1ejBWckJPZG1LeGV0NFBQVjJ0dVFsSXF1cFBpZDlScER2Q1VUTXZHazFsMGtFSnlrd1p6dkovV1VRQWYrbXNPVU5vNnY2emIrbFRkaHpmMHdGV0VuSFZSalBDbmkxekpPcGNPc3dITmw5TWdqYlRtTlhROVlLMnQ0Y0swYXhibTVnU0thSEtsY2FaNnhPVkdJYnNJN2Y3ZnQ0dXZxdzhhdlFXTjVhcW5zTkxtVWgwNUxTSUhSODhqTXRRODJqV0xVUXRJT2VoRUdPUWhVWUt4aEhIVmE0cERuT3c4Vm1LaHF6dUxJdkVQa1JTZU94R3VVTkRQOG12TFIzY2czVG89Jld6UVNoa0YvODV0THJ2c3dFSVNsengxc1Q3ST0=");
        return headers;
    }
}

