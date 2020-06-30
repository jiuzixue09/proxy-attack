package com.bigbigwork.util;

import com.bigbigwork.vo.Configure;

import java.io.File;
import java.io.IOException;

public class ConfigUtil {
    private static final String CONFIG_FILE = "config.bin";
    private static File file = new File(CONFIG_FILE);
    private static Configure configure;

    static {
        File file = new File(CONFIG_FILE);
        if(!file.exists()){
            try {
                ConfigUtil.getDefault();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {
                configure = (Configure) ObjectFileUtil.read(file);
            }catch (Exception e){
                try {
                    getDefault();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static Configure getConfigure() throws Exception {
        if(null != configure){
            return configure;
        }
        return (Configure) ObjectFileUtil.read(file);
    }

    public static void updateConfigure(Configure configure) throws IOException {
        ConfigUtil.configure = configure;
        ObjectFileUtil.write(file,configure);
    }


    public static Configure getDefault() throws IOException {
        Configure configure = new Configure();
        configure.setStartTime("08:00");
        configure.setStopTime("23:00");
        configure.setPeriod(1);
        configure.setErrorPeriod(10);
        configure.setRequests(30);
        configure.setSleepTime(5);
        configure.setFindAPPScript("tasklist |findstr \"vrr\"");
        configure.setFindProxyScript("netstat -nao |findstr ");
        configure.setStartAPPScript("\"C:\\Program Files (x86)\\BMaster\\BMaster.exe\"");
        configure.setStopAPPScript("taskkill /f /im BMaster.exe\n" +
                "taskkill /f /im v2ray_privoxy.exe\n" +
                "taskkill /f /im vrns.exe\n" +
                "taskkill /f /im vrr.exe\n" +
                "taskkill /f /im CefSharp.BrowserSubprocess.exe");
        updateConfigure(configure);
        return configure;
    }
}
