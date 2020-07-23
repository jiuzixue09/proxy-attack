package com.bigbigwork.services;


import com.bigbigwork.util.HttpClientTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PinterestRegisterService extends BaseService {
    protected final Logger ACCOUNT_LOG = LoggerFactory.getLogger("accountLog");
    private static final Pattern HTTP_STATUS_PAT = Pattern.compile("\"http_status\":\\s*([0-9]+)");
    private static Random random = new Random();
    private static final String REGISTER_URL = "https://www.pinterest.com/resource/UserRegisterResource/create/";

    Map<String, String> headers = getHead();

    public PinterestRegisterService(Consumer<String> print) {
        super(print);
        serviceName = "pinterest 用户注册：";
    }

    public static String createUserName(int minLength, int maxLength){
        StringBuilder sb = new StringBuilder();
        int length = random.nextInt(maxLength) + 1;
        if(length < minLength) length = minLength;
        for (int i = 0; i < length; i++) {
            sb.append((char) (97 + random.nextInt(26)));
        }
        return sb.toString();
    }

    public void process(){
        int times = 0;
        for (int i = 0; i < getRequests() && run.get(); i++) {
            try {
                String userName = createUserName(8,15) + "@163.com";
                String password = "123456-- asdf";
                Map<String, String> registerParams = getRegisterParams(userName, password);
                String content = HttpClientTools.doPostMap(REGISTER_URL, registerParams, headers);

                if(null != content && !content.isEmpty()){
                    Matcher matcher = HTTP_STATUS_PAT.matcher(content);
                    if(run.get()){
                        if(matcher.find()){
                            String status = matcher.group(1);
                            message(String.format("用户名：%s  注册状态:%s", userName, status));
                            if(status.equals("429")){
                                if(++times >= 5) break;
                            }else{
                                if(status.equals("200")){
                                    String pwd = URLEncoder.encode(password,"utf-8");
                                    new Thread(() ->{
                                        try{
                                            login(userName,pwd);
                                        }catch (Exception e){
                                            message("访问数据库失败！！！");
                                        }
                                    }).start();

                                    ACCOUNT_LOG.info("username={},password={}", userName, password);
                                }
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


    private static Map<String, String> getRegisterParams(String email,String password) {
        Map<String, String> params = new HashMap<>();
        String name = email.split("@")[0];
        params.put("source_url","/");
        params.put("data", String.format("{\"options\":{\"container\":\"home_page\",\"email\":\"%s\",\"password\":\"%s\",\"age\":\"33\",\"country\":\"US\",\"signupSource\":\"homePage\",\"first_name\":\"%s\",\"last_name\":\"\",\"hybridTier\":\"open\",\"page\":\"home\",\"recapToken\":\"default\",\"user_behavior_data\":\"{}\"},\"context\":{}}", email,password,name));
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
        headers.put("x-csrftoken","4e005631bf57c0d97d4a073f1f4d299a");
        headers.put("origin","https://www.pinterest.com");
        headers.put("sec-fetch-site","same-origin");
        headers.put("sec-fetch-mode","cors");
        headers.put("referer","https://www.pinterest.com/");
        headers.put("cookie","_routing_id=\"fa7975d3-bf54-4673-affd-52ad6c1b529b\"; csrftoken=4e005631bf57c0d97d4a073f1f4d299a; _pinterest_sess=TWc9PSZ3N1RTOE9Wdmw3eXR2M2NKSko0R3gzclF1T2ZrK0tKRkg4bngvMDRKejRoZVNjUW9DeFp3TUQ1Y204Q0tJdTdQSkJ0bnc2YmNDSmhmSjdjMGlpbnVodDRHOWFOdVh0aytkWi9heTV2VWp3ZjVzQk1DeDRmWXVHQ1RtaStoUG44UTA4STJoTTdqU21Jazh4RkRiY1hkc0t2dWpJNlU5OUxEWTB0YXg4dmYyMmlJQW9IQllsQ29KS0U5TWlTckszcHkmMVFra1NPQkJxbDZqYnlSSG1rZnJ4Z0lOU3E4PQ==; _auth=0; sessionFunnelEventLogged=1");
        return headers;
    }
}

