package com.bigbigwork.controllers;

import com.bigbigwork.services.*;
import com.bigbigwork.timer.Timer;
import com.bigbigwork.util.HttpClientTools;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainController extends BaseController {
    @FXML
    public TextArea logContent;
    @FXML
    public ListView<String> dataType;

    @FXML
    public MenuBar topPane;
    @FXML
    public ToolBar toolBar;
    //流量控制
    private static final int MAX_SIZE = (int)(1000000000 * 0.9);

    private final List<AppService> tasks = new ArrayList<>();
    private final List<String> names = new ArrayList<>();
    AppRestartService appRestartService = new AppRestartService(this::print);
    FindRealIpService realIpService = new FindRealIpService(this::print);
    FindAppProxyIdService appProxyIdService = new FindAppProxyIdService(this::print);

    public static volatile boolean running = false;

    public MainController() {
        Timer timer = new Timer(this);
        new Thread(timer).start();
        autoRun();
    }

    /**
     * 自动化脚本流量攻击
     */
    public void autoRun(){
        String autot_raffic_attack = System.getProperty("AUTOT_RAFFIC_ATTACK");
        if(Objects.nonNull(autot_raffic_attack) &&
                (autot_raffic_attack.equalsIgnoreCase("yes") ||
                        autot_raffic_attack.equalsIgnoreCase("true"))){
            login();
            traffic();
        }
    }

    public void traffic() {
        try {
            AtomicLong totalSize = new AtomicLong(0);
            Path path = Paths.get("behance_url.txt");
            List<String> lines = Files.readAllLines(path);
            Random random = new Random();

            for (int i = 0; i < 10; i++) {
                new Thread(() -> {
                    int SSLHandshakeExceptionTimes = 0;
                    for (int j = 0; j < 100; j++) {
                        String url = lines.get(random.nextInt(lines.size()));
                        try {
                            long size = HttpClientTools.download(url, null);
                            if(size == -1){
                                SSLHandshakeExceptionTimes += 1;
                                if(SSLHandshakeExceptionTimes > 2){
                                    print("代理异常， 程序停止！！！" );
                                    break;
                                }
                            }else{
                                SSLHandshakeExceptionTimes = 0;
                            }
                            if(totalSize.addAndGet(size) > MAX_SIZE){
                                print("流量使用完毕！！！");
                                break;
                            }
                            if(size > 0.1){
                                print(String.format("已用流量：%.2fM", (totalSize.get() / 1000000.0)));
                            }
                        }catch (Exception e){
                            print("文件下载失败！！！");
                            LOG.error("文件下载失败 : url={}", url ,e);
                        }
                    }
                }).start();
            }

        }catch (Exception e){
            print("自动化流量攻击异常: " + e.getMessage());
            LOG.error("自动化流量攻击异常", e);
        }
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

    Pattern pat = Pattern.compile("name\":[\"]?([^\",]+).*password\":\"([^\"}]+)");
    public void login() {

        try {
            String account = BMasterAccountService.getAccount();
            if(Objects.nonNull(account) && account.length() > 0){
                BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);
                Matcher matcher = pat.matcher(account);
                if(matcher.find()){
                    queue.add(matcher.group(1));
                    queue.add(matcher.group(2));
                    new LoginService(new File("record.bin")).login(queue);
                }else{
                    print("账号解析出错：" + account);
                }
            }
        } catch (InterruptedException e) {
            print("APP账号获取失败：" + e.toString());
            e.printStackTrace();
        }
    }

    public void trafficAttack() {
        try{
            appRestartService.startPython();
            HttpClientTools.doGet("http://localhost:5000/pinterest/scroll");
        }catch (Exception e){
            print("流量攻击异常:" + e.toString());
            LOG.error("traffic attack error", e);
        }
    }
}
