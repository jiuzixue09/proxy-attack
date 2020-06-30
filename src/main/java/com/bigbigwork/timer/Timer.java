package com.bigbigwork.timer;

import com.bigbigwork.controllers.MainController;
import com.bigbigwork.services.AppRestartService;
import com.bigbigwork.util.ConfigUtil;
import com.bigbigwork.vo.Configure;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class Timer implements Runnable {

    private MainController mainController;
    private AppRestartService appRestartService;
    Configure configure;

    public Timer(MainController mainController) {
        this.mainController = mainController;
        appRestartService = new AppRestartService(mainController::print);
        init();
    }

    private void init(){
        try {
            this.configure = ConfigUtil.getConfigure();
        } catch (Exception e) {
            mainController.print("读取配置文件异常\n" + e);
        }
    }

    public void run(){
        while (true){
            try {
                String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                if(time.equals(configure.getStopTime()) && MainController.running){
                    mainController.print(configure.getStopTime() + " 停止服务");
                    mainController.stopService();
                    appRestartService.stopApp();
                }

                if(time.equals(configure.getStartTime()) && !MainController.running){
                    mainController.print(configure.getStartTime() + "启动服务");
                    appRestartService.startApp();
                    mainController.startService();
                }
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }
}
