package com.bigbigwork.services;

import com.bigbigwork.util.ConfigUtil;
import com.bigbigwork.util.ExecuteUtil;
import com.bigbigwork.vo.Configure;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class AppRestartService {
    private Consumer<String> print;
    Configure configure;

    public AppRestartService(Consumer<String> print) {
        this.print = print;
        try {
            configure = ConfigUtil.getConfigure();
        } catch (Exception e) {
            print.accept("读取配置文件异常: "+ e.getMessage());
            e.printStackTrace();
        }
    }

    public void restartApp() {
        stopApp();
        startApp();
    }

    public void startApp(){
        runBat(configure.getStartAPPScript(), false);
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {

        }
    }
    public void stopApp(){
        Stream.of(configure.getStopAPPScript().split("\n")).forEach(it -> runBat(it,true));
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {

        }
    }

    public void stopFirefox(){
        try {
            runBat("taskkill /im firefox.exe",true);
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ignored) {

        }
    }

    private void runBat(String command, boolean wait){
        print.accept(command);

        if(wait){
            String rs = ExecuteUtil.exec(command);
            print.accept(rs);
        }else{
            try {
                ExecuteUtil.execWithOutMessage(command);
            } catch (IOException e) {
                print.accept(e.toString());
            }
        }

    }
}
