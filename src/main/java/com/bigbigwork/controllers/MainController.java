package com.bigbigwork.controllers;

import com.bigbigwork.services.*;
import com.bigbigwork.timer.Timer;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class MainController extends BaseController {
    @FXML
    public TextArea logContent;
    @FXML
    public ListView<String> dataType;

    @FXML
    public MenuBar topPane;
    @FXML
    public ToolBar toolBar;

    private final List<AppService> tasks = new ArrayList<>();
    private final List<String> names = new ArrayList<>();
    AppRestartService appRestartService = new AppRestartService(this::print);
    FindRealIpService realIpService = new FindRealIpService(this::print);
    FindAppProxyIdService appProxyIdService = new FindAppProxyIdService(this::print);

    public static volatile boolean running = false;

    public MainController() {
        Timer timer = new Timer(this);
        new Thread(timer).start();
    }

    @FXML
    public void initialize() {
        realIpService.setNames(names);
        dataType.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        topPane.prefWidthProperty().bind(boarderPane.widthProperty());
        toolBar.prefWidthProperty().bind(boarderPane.widthProperty());

    }


    public synchronized void print(String s) {
        Platform.runLater(() -> {
                    if (logContent.getText().length() > 8000) {
                        logContent.setText(logContent.getText().substring(7000));
                    }
                    logContent.appendText(rightNow.get() + ":" + s + "\n");
                    LOG.info(s);
                }

        );

    }


    public void startService() {
        tasks.clear();
        names.clear();

        ObservableList<String> selectedItems = dataType.getSelectionModel().getSelectedItems();
        for (String selectedItem : selectedItems) {
            AppService task = getTask(selectedItem);
            if(null != task){
                tasks.add(task);
                names.add(selectedItem);
            }
        }

        if(tasks.isEmpty()){
            print("请选择一个任务开始运行");
            return;
        }

        if(running) {
            print("服务正在运行中...");
            return;
        }

        start();

    }

    public AppService getTask(String name){
        switch (name){
            case "Pinterest 登录":
                return new PinterestLoginService(this::print);
            case "Pinterest 注册":
                return new PinterestRegisterService(this::print);
            case "Pinterest 搜索":
                return new PinterestSearchService(this::print);
            case "Behance 搜索":
                return new BehanceSearchService(this::print);
        }
        return null;
    }

    private boolean proxyHandle(){
        for (int i = 0; i < 3 && running; i++) {
            try {
                Boolean proxyIp = showProxyIp();
                if (!proxyIp) {
                    print("代理IP 获取异常，重启APP");
                    if( i == 2){
                        print("无法获取代理IP, 程序暂停！");
                        return false;
                    }
                    TimeUnit.MINUTES.sleep(1);
                }else{
                    return true;
                }
            } catch (Exception e) {
                print("获取代理IP异常");
            }
        }
        return false;
    }

    private void checkProxyIP(){
        new Thread(() ->{
            while (running){
                try {
                    if (!proxyHandle()) stop();
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    private void process() {
        new Thread(this::checkProxyIP).start();

        tasks.forEach(appService -> {
            try {
                new Thread(() -> {
                    while (running) {
                        appService.run();
                    }
                }).start();
            } catch (Exception e) {
                try {
                    TimeUnit.MINUTES.sleep(2);
                } catch (InterruptedException ignored) {
                }
            }
        });

    }


    public void stopService() {
        if(!running) {
            print("服务未启动...");
            return;
        }

        stop();
    }

    private void stop(){
        running = false;
        tasks.forEach(AppService::stop);
        try {TimeUnit.SECONDS.sleep(1); } catch (InterruptedException ignored) {}
        print("停止服务");
    }

    private void start(){
        running = true;
        tasks.forEach(AppService::start);
        print("开始服务");
        new Thread(this::process).start();
    }

    public void clear() {
        logContent.clear();
    }

    public void restartApp() {
        appRestartService.restartApp();
    }

    public void stopApp(){
        appRestartService.stopApp();
    }
    public void startApp(){
        appRestartService.startApp();
    }


    public Boolean showProxyIp() {
        return realIpService.call();
    }

    public void showAppProxyIp(){
        appProxyIdService.run();
    }

    public void login() {
        new LoginService(new File("record.bin")).login();
    }
}
