import com.bigbigwork.services.PinterestRegisterService;
import com.bigbigwork.util.HttpClientTools;

import java.util.HashMap;
import java.util.Map;

public class PinterestLoginTest {
    private static final String LOG_IN_URL = "https://www.pinterest.com/resource/UserSessionResource/create/";

    private static Map<String, String> getLogInParams(String userName) {
        Map<String, String> params = new HashMap<>();
        params.put("source_url","/");
        params.put("data", String.format("{\"options\":{\"username_or_email\":\"%s\",\"password\":\"123456-- asdf\",\"app_type_from_client\":5,\"visited_pages_before_login\":null,\"tokenV3\":\"default\"},\"context\":{}}", userName));
        return params;
    }

    public static void main(String[] args) {
        String userName = "xzcevranespx@163.com";
        Map<String, String> params = getLogInParams(userName);
        Map<String, String> headers = getHead();
        HttpClientTools.doPostCookie(LOG_IN_URL, params, headers);

    }

    //

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
        headers.put("cookie","csrftoken=fdb48c9784a30c1478142ace8fa936c9;");
        return headers;
    }
}
