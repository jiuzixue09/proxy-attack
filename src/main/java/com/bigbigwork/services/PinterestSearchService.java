package com.bigbigwork.services;


import com.bigbigwork.util.HttpClientTools;

import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class PinterestSearchService extends BaseService {
    private static final Pattern NEXT_BOOKMARK_PAT = Pattern.compile("bookmark\":\"([^\"]{200,})");
    private static final Pattern HTTP_STATUS_PAT = Pattern.compile("\"http_status\":\\s*([0-9]+)");
    private final static String PRE_URL = "https://www.pinterest.com/resource/BaseSearchResource/get/?data=";

    private static final List<String> keywords = new ArrayList<>();

    public PinterestSearchService(Consumer<String> print) {
        super(print);
        serviceName = "pinterest 搜索：";
        if (keywords.isEmpty()) {
            synchronized (keywords) {
                if (keywords.isEmpty()) {
//                    keywords.addAll(Arrays.asList("portrait","women","kid","landscape","animal","cat","dog","business","girl","cartoon"));
                    Path path = Paths.get("behance.txt");
                    try {
                        LOG.info("read file:{}", path.toFile().getAbsolutePath());
                        keywords.addAll(Files.readAllLines(path));
                        LOG.info("read file finished, size={}", keywords.size());
                    } catch (Exception e) {
                        LOG.error("can't find file:{}", path.toFile().getAbsolutePath());
                        message("读取文件异常：" + path + "\n" + e.getMessage());
                    }
                }
            }
        }

    }

    public void process() {
        Map<String, String> head = getHead();
        if (keywords.isEmpty()) {
            message("无搜索关键词");
            return;
        }

        String keyword = keywords.get(random.nextInt(keywords.size()));
        String url = getUrl(keyword);

        for (int i = 0; i < getRequests() && run.get() && null != url; i++) {
            try {
                LOG.info(url);
                String content = HttpClientTools.doGet(url,head);
                if (null != content && !content.isEmpty()) {

                    Matcher matcher = HTTP_STATUS_PAT.matcher(content);
                    if (matcher.find()) {
                        String status = matcher.group(1);
                        message("搜索关键字： " + keyword + ",第（" + (i + 1) + "）页, 状态为：" + status);
                        if(!status.equals("200")) break;

                        matcher = NEXT_BOOKMARK_PAT.matcher(content);
//                        LOG.info(content);
                        if (run.get() && matcher.find()) {
                            url = getNextUrl(matcher.group(1), keyword);
                        }else{
                            break;
                        }

                    } else {
                        message("接口获取状态失败");
                        onError();
                    }

                } else {
                    message("接口获取内容失败");
                    onError();
                }


            } catch (Exception e) {
                LOG.error("error:", e);
            } finally {
                afterRequest();
            }

        }
    }

    private String getNextUrl(String bookmark, String keyword){
        String part = "{\"options\":{\"bookmarks\":[\"%s\"],\"query\":\"%s\"}}";
        try {
            return PRE_URL + URLEncoder.encode(String.format(part, bookmark, keyword), "utf-8");
        }catch (Exception e) {
            LOG.error("error", e);
        }

        return null;
    }


    private String getUrl(String keyword) {
        String part = "{\"options\":{\"isPrefetch\":false,\"query\":\"%s\",\"scope\":\"pins\"},\"context\":{}}";
        try {
            return PRE_URL + URLEncoder.encode(String.format(part, keyword), "utf-8");
        } catch (Exception e) {
            LOG.error("error", e);
        }

        return null;
    }

    private static Map<String, String> getHead() {

        Map<String, String> headers = new HashMap<>();
        headers.put("Host","www.pinterest.com");
        headers.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:74.0) Gecko/20100101 Firefox/74.0");
        headers.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Language","zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        headers.put("Accept-Encoding","gzip, deflate");
        headers.put("Connection","close");
//        headers.put("Cookie","csrftoken=c44bc004e746e1911df509a0870f2dcf; _pinterest_sess=TWc9PSZFdlBoN1FwOXlQUkFGL043cTFyb0oraTN3VHRpTHRoQ0dDQ2J4bEFEL3hod3ExeFdZNzl5enVsS251aVBGQ2VwTGJkeXhrWHRHVkF6cGxxdU5NRDVaSWlBdExORFBmeDdUUFpnTC9WQmozSG1SVWRVVHB5RlpQem5KdnVTRTVBbGZXZDVvR2dMeEc0L3RQSDZXRjN1eDhNOHFZT1BaZGpEYi9hVnRncHdqS1k9JmdpRUtOa3NBMHI4VUhQV2tIRFJoZmtmcFNFVT0=; _auth=0; G_ENABLED_IDPS=google; _pinterest_cm=TWc9PSZlODJzbDlSaHM3bnc2RWdYdjdZZjlFTDU2eW9FellIOEltNlJnSGNTdDV4OHVCdXFtL3dHVVkxK1dVYTNjVGJxOFY1MzlGNnhFMERCWU1ka1FUQ1RySklzN1BDWVVnQjA0RkxrUERNQndLYnlKbDBzYWVSclhEajQ3ZmtzZkF4TCZTZTM1YldyM0hrRGwvcjhQVVJTUTduL2sydEE9; _routing_id=\"e1e162df-2356-4e23-aa79-dba7ed1912c0\"; sessionFunnelEventLogged=1");
        headers.put("Cookie","csrftoken=6931b1f4a925b57cc884af8f6f5a3bb1; _pinterest_sess=TWc9PSZyRXA1UHNaOTVyNWF1QnhEbjlGL1ErdUVXU2ZOdjhQMWo4c3g5V1pqNzF3WUptUERNSlpNMnBUa3hDL1lZODZLZm8vZStnVXNOaXVGc0NlbGkrbHFhOWpZQ2hEeFp0L2k4Q1NEa2JHbXp0TUV3ZHVQbGk0Y29JaE9oNVk4QmU1L1JSVFF3OExCOGJhNHBQZkJrNU9EM1VZT0dFODRWeVJHOEh2a1JrYXVMQmJZNmFyNDRwZytBZkcyajF2Z2NNcVomdVFSNFRRNUtYYmZsRkY2UXJVdzRHQng4ekNjPQ==; _auth=0; _routing_id=\\\"07374683-d0e4-4240-873f-f3782c36b3ab\\\"; sessionFunnelEventLogged=1; hasBeenShownLoginModalInOpen=true");
//        headers.put("Cookie","csrftoken=0fd3e0f6c0044020c57271436dcc0983; _pinterest_sess=TWc9PSZxWWxHUFVVR3BBenFaWFo4dGpCeXRNUzZQVytwWUc2a0FoMzd3WUdPbG1HNVdFUjlNZnM2RkVPWCt6azMrNGVzZGM4Ry9SSG8wSzFLbXhFbFE3eWQ2b29JQ1dnY1lxUUgzUkM4cDZuWkUrcDhJQkovdWFzODJnTnVsNzdPK3MzTTYrRzg4MkpDY3ZMcHRGS2JjNVJ1SmY4bzFlaHloRndvL0o1YXJra0VHYzlQVERySjdvY3Y4ZS9wQzdMU2J5YWx3UUpOTFV4ZFFJeFB0TlpGZlhTeC9rcmJBZzI4RmE2Y3kyNVQ4anVPWkZtamhWb1F4K0RMbjRyajlyVzI4YWdRRytlR0REd25keG0rWm9vZkZBUUNXc1JjZ0ZBZ0pZcHVGRElMMmk1UUpUNDQyOGdqNE8wdWFmOTZQa0U1bzl5eXB3NVAxWGNVVTNUWVpPcVRqekh2djU3L2IxMVdKazVpVUhGdEM5MmROY3d5eUkrOVpzMzJmR3F5TjVVTFBGUkVEb2UxLzFlWTlFUk8vMWo1Qk9RR0tyUXVMUklBTzZUNzlTSkFVUHdkZEFOd2hhNmdzNVdQenprbjYvTXFXbzU3M3BUMzBUSk93bERzanIrQ0FpWVRzcWJlRUg4bzZRcHROcGFsNUxlYk9jMXFNTjEyeUdsOVl2Z2tiYXhuR0VoUSttdmNMRjgySzh4NVNYN041VTRra1pUYmZac2lqbVVDRk8rR3lyNVlIOFg3MWF2dEJGd2JIRHpiYy9MbGhBaU1SbTJicWR1THJBVGpXaC80REZoZ3VGbS9EalR2STRqcXVYTVJtQ0RuenVDMi9CWWsxd0k1M2RBZWN4QWhnVVd0Y0srR3NlK2tTK2trcUlCcmtlWHJyM2xvWHk3OFFCZzkrRy9EYTljdmhDcXJscXlENE9CZ1A4WnhDWjZYUGMrNGR1RzIzNXlwY3NvaS96NjBOeWRlNFRFTkY0a2s3QnVaTk96THpLUjlja2FBS2hLbHlmVXliVElybFZMd2Fzcmc5R3lRcE1GQzFQeUpyanJqWGlmRitoM1R6d0V4QlorbjdPVXBpc0cxT3l4cVA5Vk5oQ25PWTNPbmlJOUlNc0RMJnBPbFd3YUNRM210YUZ4NG1JOU9Sck9INlhJST0=; _auth=1; _routing_id=\"ba500fce-8f79-4152-a134-b124a6604ca8\"; sessionFunnelEventLogged=1; hasBeenShownLoginModalInOpen=true; _b=\"AUzUBqinp55A3Z1Njdmj/LUFRvBczIVoSeMFarD68Lb/OFwWaqYRudGdCs4Ocx/GggQ=\"; bei=false");
        headers.put("Upgrade-Insecure-Requests","1");
        headers.put("Pragma","no-cache");
        headers.put("Cache-Control","no-cach");
        return headers;
    }

}

