package com.bigbigwork.services;

import com.bigbigwork.util.ConfigUtil;
import com.bigbigwork.util.ExecuteUtil;
import com.bigbigwork.vo.Configure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindAppProxyIdService implements Runnable {

    private static final Pattern taskPat = Pattern.compile(".+\\s+([0-9]+)");
    private static final Pattern ipPat = Pattern.compile("TCP\\s+([0-9.]+):[0-9]+\\s+([0-9.]+:[0-9]+)");
    private Consumer<String> print;
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    protected final Logger IP_LOG = LoggerFactory.getLogger("ipLog");
    Configure configure;

    public FindAppProxyIdService(Consumer<String> print) {
        this.print = print;

        try {
            configure = ConfigUtil.getConfigure();
        } catch (Exception e) {
            print.accept("读取配置文件异常: "+ e.getMessage());
            e.printStackTrace();
        }
    }

    public Optional<String> findTask(String command){
        Optional<String> optional = Optional.empty();
        String exec = ExecuteUtil.exec(command);
        LOG.info("command={}\n result={}",command, exec);
        Matcher matcher = taskPat.matcher(exec);
        if(matcher.find()){
            optional = Optional.of(matcher.group(1));
        }
        return optional;
    }

    Predicate<String> filter = str -> str.startsWith("0.") || str.startsWith("127.");

    public Set<String> findIpAndPort(String command){
        LOG.info(command);
        String exec = ExecuteUtil.exec(command);
        LOG.info(exec);
        Matcher matcher = ipPat.matcher(exec);
        Set<String> list = new HashSet<>();
        while(matcher.find()){
            String group = matcher.group(2);
            if(filter.test(group)) continue;
            if(list.add(group)){
                IP_LOG.info(group);
            }
        }
        return list;
    }

    private Optional<String> createCommand() throws IOException {
        Optional<String> optional = Optional.empty();

        String taskCommand =  configure.getFindAPPScript();
        Optional<String> port = findTask(taskCommand);

        if(port.isPresent()){
            String ipCommand = configure.getFindProxyScript();
            optional = Optional.of(ipCommand.concat(" \"").concat(port.get()).concat("\""));
        }else{
            print.accept("找不到进程:" + taskCommand);
        }

        return optional;
    }


    @Override
    public void run() {
        try {
            if(createCommand().isPresent()){
                String command = createCommand().get();
                Set<String> ipAndPort = findIpAndPort(command);
                ipAndPort.forEach(it -> print.accept("代理IP信息：" + it));
            }
        } catch (IOException e) {
            LOG.error("命令异常", e);
            print.accept("命令异常：" + e.getMessage());
        }

    }
}
