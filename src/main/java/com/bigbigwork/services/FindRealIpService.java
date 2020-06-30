package com.bigbigwork.services;

import com.bigbigwork.util.HttpClientTools;
import com.bigbigwork.util.IpUtil;
import com.bigbigwork.util.JedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindRealIpService implements Callable<Boolean> {

    private static final String url = "https://www.pinterest.com/resource/BoardFeedResource/get/";
    private static final Pattern ipPat = Pattern.compile("\"real_ip\":\\s*\"([^\"]+)");
    private Consumer<String> print;
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    protected final Logger IP_LOG = LoggerFactory.getLogger("ipLog");
    private List<String> names;

    public void setNames(List<String> names) {
        this.names = names;
    }

    public FindRealIpService(Consumer<String> print) {
        this.print = print;
    }


    public String findIp(){
        String content = HttpClientTools.doGet(url);
        if(null == content || "".equals(content)){
            LOG.info("content is empty");
            return null;
        }
        Matcher matcher = ipPat.matcher(content);
        if(matcher.find()){
            return matcher.group(1);
        }
        return null;
    }

    @Override
    public Boolean call() {
        String ip = findIp();
        if (null != ip) {
            print.accept("代理IP信息：" + ip);
            IP_LOG.info(ip);
            JedisUtil jedisUtil = JedisUtil.getInstance();
            JedisUtil.Sets sets = jedisUtil.new Sets();
            String key = "3big:proxy:info:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            for (String name : names) {
                try {
                    sets.sadd(key,name + ":" + IpUtil.getLocalIpv4Address() + "->" + ip);
                }catch (Exception e){
                    sets.sadd(key,name + ":" + ip);
                }
            }

            key = "3big:proxy:ip:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            sets.sadd(key, ip);


            return true;
        }
        return false;
    }
}
