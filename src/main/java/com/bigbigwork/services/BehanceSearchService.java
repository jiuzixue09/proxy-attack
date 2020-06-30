package com.bigbigwork.services;


import com.bigbigwork.util.HttpClientTools;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BehanceSearchService extends BaseService {
    Map<String, String> headers = getHead();
    private static final List<String> keywords = new ArrayList<>();

    public BehanceSearchService(Consumer<String> print) {
        super(print);
        serviceName = "behance 搜索：";
        if(keywords.isEmpty()){
            synchronized (keywords){
                if(keywords.isEmpty()){
                    Path path = Paths.get("behance.txt");
                    try {
                        LOG.info("read file:{}", path.toFile().getAbsolutePath());
                        keywords.addAll(Files.readAllLines(path));
                        LOG.info("read file finished, size={}", keywords.size());
                    }catch (Exception e){
                        LOG.error("can't find file:{}", path.toFile().getAbsolutePath());
                        message("读取文件异常：" + path + "\n" + e.getMessage());
                    }
                }
            }
        }
    }

    public void process(){
        if(keywords.isEmpty()){
            message("无搜索关键词");
            return;
        }
        for (int i = 0; i < getRequests() && run.get(); i++) {
            try {
                String keyword = keywords.get(random.nextInt(keywords.size()));
                keyword = URLEncoder.encode(keyword,"utf-8");
                String url = "https://www.behance.net/search?content=projects&search=" + keyword + "&sort=published_date&time=month&ordinal=" + (random.nextInt(1000) + 10);

                int status = HttpClientTools.doGetStatus(url, headers);
                LOG.info("url={}, status={}", url, status);
                if(run.get()){
                    message("状态为：" + status);
                }

            }catch (Exception e){
                message("接口异常");
                onError();
            }finally {
                afterRequest();
            }
        }

    }

    private List<String> getKeywords(String path) {
        List<String> keywords;
        try {
            keywords = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            message("文件不存在:" + path);
            return null;
        }
        return keywords;
    }


    private static Map<String, String> getHead() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Host","www.behance.net");
        headers.put("Accept","*/*");

        return headers;
    }
}

