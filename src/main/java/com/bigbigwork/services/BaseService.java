package com.bigbigwork.services;

import com.bigbigwork.util.ConfigUtil;
import com.bigbigwork.util.DBUtil;
import com.bigbigwork.vo.Configure;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class BaseService implements AppService {
    protected String serviceName;
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    protected static Random random = new Random();
    protected Configure configure;

    protected AtomicBoolean run = new AtomicBoolean(true);

    protected Consumer<String> print;

    public void start(){
        run.set(true);
    }

    public void stop(){
        run.set(false);
    }

    public BaseService(Consumer<String> print) {
        this.print = print;
    }

    protected int getRequests(){
        return configure.getRequests();
    }

    public abstract void process();

    public void run(){
        beforeProcess();
        process();
        afterProcess();
    }

    public void onError(){
        if(configure.getErrorPeriod() > 0){
            try {TimeUnit.SECONDS.sleep(configure.getErrorPeriod()); } catch (InterruptedException ignored) {}
        }
    }

    public void message(String message){
        message = serviceName + message;
        if(null != print){
            print.accept(message);
        }else{
            LOG.info(message);
        }
    }

    public void beforeProcess(){
        try {
            configure = ConfigUtil.getConfigure();
        } catch (Exception e) {
            print.accept("读取配置文件异常");
            try {
                configure = ConfigUtil.getDefault();
            } catch (IOException ex) {
                print.accept("创建配置文件异常:" + e);
            }
        }
    }

    public void afterRequest(){
        if(configure.getPeriod() > 0){
            try {TimeUnit.SECONDS.sleep(configure.getPeriod()); } catch (InterruptedException ignored) {}
        }
    }

    public void afterProcess(){
        if(configure.getSleepTime() > 0){
            int timeout = random.nextInt(configure.getSleepTime()) + 1;
            if(run.get()) {
                print.accept(serviceName + "休息" + timeout + "分钟");
                try {TimeUnit.MINUTES.sleep(timeout);}catch (Exception ignored){}
            }
        }

    }

    ComboPooledDataSource dataSource = DBUtil.getDataSource();
    private static final String sql = "insert into `pinterest-test`.account_cookie(user_name,user_password) values(?,?)";

    public void login(String userName, String password){

        try (Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,userName);
            preparedStatement.setString(2,password);
            preparedStatement.executeUpdate();
        }catch (Exception e){
            print.accept("sql error:" + e);
        }

    }
}
