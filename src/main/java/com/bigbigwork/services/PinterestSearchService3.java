package com.bigbigwork.services;


import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PinterestSearchService3 implements Runnable  {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    protected static Random random = new Random();
    private static final Pattern NEXT_BOOKMARK_PAT = Pattern.compile("bookmark\":\"([^\"]+)");
    private static final Pattern HTTP_STATUS_PAT = Pattern.compile("\"http_status\":\\s*([0-9]+)");
    private final static String PRE_URL = "https://www.pinterest.com/resource/BaseSearchResource/get/?data=";

    private static final List<String> keywords = new ArrayList<>();

    public PinterestSearchService3() {
        if (keywords.isEmpty()) {
            synchronized (keywords) {
                if (keywords.isEmpty()) {
                    Path path = Paths.get("behance.txt");
                    try {
                        LOG.info("read file:{}", path.toFile().getAbsolutePath());
                        keywords.addAll(Files.readAllLines(path));
                        LOG.info("read file finished, size={}", keywords.size());
                    } catch (Exception e) {
                        LOG.error("can't find file:{}", path.toFile().getAbsolutePath());
                    }
                }
            }
        }

    }

    public static void main(String[] args) {
        for (int i = 0; i < 20; i++) {
            new Thread(new PinterestSearchService3()).start();
        }
    }

    public void run() {
        Map<String, String> head = getHead();
        if (keywords.isEmpty()) {
            return;
        }

        while(true){
            String keyword = keywords.get(random.nextInt(keywords.size()));
            String url = getUrl(keyword);

            for (int i = 0; i < 30; i++) {
                try {
                    LOG.info(url);
                    String content = doGet(url,head);
                    if (null != content && !content.isEmpty()) {

                        Matcher matcher = HTTP_STATUS_PAT.matcher(content);
                        if (matcher.find()) {
                            String status = matcher.group(1);
                            LOG.info("搜索关键字： " + keyword + ", 状态为：" + status);
                            if(!status.equals("200")) break;

                            matcher = NEXT_BOOKMARK_PAT.matcher(content);
                            if (matcher.find()) {
                                url = getNextUrl(matcher.group(1), keyword);
                            }else{
                                break;
                            }

                        } else {
                            LOG.info("接口获取状态失败");
                        }

                    } else {
                        LOG.info("接口获取内容失败");
                    }


                } catch (Exception e) {
                    LOG.error("error:", e);
                }

            }
        }

    }

    private static final int TIME_OUT = 1000 * 60;
    public static String doGet(String url, Map<String, String> headers) {

        CloseableHttpClient httpclient = HttpClients.custom()
                .build();
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIME_OUT).setConnectTimeout(TIME_OUT)
                .build();
        HttpGet httpGet = new HttpGet(url);

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpGet.setHeader(header.getKey(), header.getValue());
            }
        }

        httpGet.setConfig(requestConfig);
        try {
            CloseableHttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, Consts.UTF_8);
        } catch (Exception e) {
            System.err.println("url=" + url + "\n" + e);
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
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
        headers.put("Cookie","csrftoken=60792b3078753d407cf7a5d168444143; _pinterest_sess=TWc9PSZMQXhSQ0ZXWFBtY3hSSHlINlA2VlN6YVRFcVNWYXlVWjZyVEozR3BwYzlmN2VDZlRLT3NyWVVPSXB3VGtBMkNpaU04V3EwVTVsYUQ4c1cxT2NVS2pNeE1WUmUvRUE0dE1Kd0ROeGlJUnR3WTFVWUQwdG9pSStYcVhCUzRyT3U3N1FST3FGaFdYbUdIL3MrY0hMN1pZZTh5WjBYQy9Vck9OaExUTGlEa1V6QWM9JklCYndGUXJFZTJaSkkxQXVIZGVQdXBSSGpsND0=; _auth=0; _routing_id=\"dc0bf0b5-61d3-408a-b711-a1abc0c7ccc5\"; sessionFunnelEventLogged=1; G_ENABLED_IDPS=google");
        headers.put("Upgrade-Insecure-Requests","1");
        headers.put("Pragma","no-cache");
        headers.put("Cache-Control","no-cach");
        return headers;
    }

}

