package com.bigbigwork.services;

import com.bigbigwork.util.KeySprite;
import com.bigbigwork.util.ObjectFileUtil;
import com.bigbigwork.vo.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoginService {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final File file;

    public LoginService(File file) {
        this.file = file;
    }

    public void login(){
        try {
            List<Command> list = (List<Command>) ObjectFileUtil.read(file);
            LOG.info(list.toString());

            list.forEach(command -> {
                String operation = command.getOperation();
                if(command.getType().equals("点击事件")){
                    String[] xy = operation.split(",");
                    int x = Integer.parseInt(xy[0].split(":")[1]);
                    int y = Integer.parseInt(xy[1].split(":")[1]);
                    try {
                        KeySprite.mouseClick(x,y);
                    } catch (AWTException e) {
                        LOG.error("click error", e);
                    }
                }else{
                    try {
                        if (operation.equals("17,86")) {
                            KeySprite.paste("123456");
                        } else if(operation.equals("20")) {
                            KeySprite.capsSwitch();
                        }else{
                            KeySprite.input(Integer.parseInt(command.getKeyCode().split(",")[0]));
                        }
                    } catch (AWTException e) {
                        LOG.error("input error", e);
                    }
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logout(){

    }
}
