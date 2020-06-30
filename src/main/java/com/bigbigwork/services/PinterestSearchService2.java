package com.bigbigwork.services;


import com.bigbigwork.util.HttpClientTools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class PinterestSearchService2 extends BaseService {
    private static final Pattern HTTP_STATUS_PAT = Pattern.compile("\"http_status\":\\s*([0-9]+)");
    private final static String SEARCH_URL = "https://www.pinterest.com/resource/BoardFeedResource/get/?source_url=";
    private static final String DATA ="/#idString#/&data={\"options\":{\"isPrefetch\":false,\"board_id\":\"#idNumber#\",\"board_url\":\"/#idString#/\",\"field_set_key\":\"react_grid_pin\",\"filter_section_pins\":true,\"layout\":\"default\",\"page_size\":5,\"redux_normalize_feed\":true},\"context\":{}}&_=";

    private static final List<String> keywords = new ArrayList<>();

    public PinterestSearchService2(Consumer<String> print) {
        super(print);
        serviceName = "pinterest 搜索：";
        if(keywords.isEmpty()){
            synchronized (keywords){
                if(keywords.isEmpty()){
                    Path path = Paths.get("pins.txt");
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

    public void process() {
        if(keywords.isEmpty()){
            message("无搜索关键词");
            return;
        }
        for (int i = 0; i < getRequests() && run.get(); i++) {
            try {
                String url = getUrl(random.nextInt(keywords.size()));

                LOG.info(url);
                String content = HttpClientTools.doGet(url);
                if(null != content && !content.isEmpty()){
                    Matcher matcher = HTTP_STATUS_PAT.matcher(content);
                    if(run.get()){
                        if(matcher.find()){
                            String status = matcher.group(1);
                            message("状态为：" + status);
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
            }
            finally {
                afterRequest();
            }

        }
    }


    private String getUrl(int index) {
        String s = keywords.get(index);
        String[] split = s.split(",");
        if(split.length == 2){
            String id_string = split[0];
            String idNumber = split[1];
            String url = DATA.replace("#idString#", id_string).replace("#idNumber#", idNumber);
            url = url.replace("/", "%2F").replace("{", "%7B").replace("}", "%7D").replace("\"", "%22").replace(":", "%3A").replace("[", "%5B").replace("]", "%5D").replace(",", "%2C")+System.currentTimeMillis();
            url = SEARCH_URL + url;
            return url;
        }

        return null;
    }

}

