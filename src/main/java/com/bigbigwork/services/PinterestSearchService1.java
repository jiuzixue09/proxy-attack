package com.bigbigwork.services;


import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;


@Deprecated
public class PinterestSearchService1 extends BaseService {
    private static final Pattern HTTP_STATUS_PAT = Pattern.compile("\"http_status\":\\s*([0-9]+)");
    private final static String PRE_URL = "https://www.pinterest.com/search/pins/?q=%s&rs=typed";
    AppRestartService service = new AppRestartService(print);


    private static final List<String> keywords = new ArrayList<>();

    public PinterestSearchService1(Consumer<String> print) {
        super(print);
        serviceName = "pinterest 搜索：";
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
                        message("读取文件异常：" + path + "\n" + e.getMessage());
                    }
                }
            }
        }


    }

    public void process() {
        if (keywords.isEmpty()) {
            message("无搜索关键词");
            return;
        }
        for (int i = 0; i < getRequests() && run.get(); i++) {
            try {
                String url = getUrl(random.nextInt(keywords.size()));
                print.accept("访问页面：" + url);
                if(Desktop.isDesktopSupported()){
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI(url));
                }

            } catch (Exception e) {
                LOG.error("error:", e);
            } finally {
                afterRequest();
            }

        }
        try {
            print.accept("关闭浏览器");
            service.stopFirefox();
        }catch (Exception e){
            print.accept("关闭浏览器异常：" + e);
        }
    }


    private String getUrl(int index) {
        String s = keywords.get(index);

        try {
            return String.format(PRE_URL, URLEncoder.encode(s, "utf-8"));
        } catch (Exception e) {
            LOG.error("error", e);
        }

        return null;
    }

}

