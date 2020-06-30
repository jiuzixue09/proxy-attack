package com.bigbigwork.services;

import com.bigbigwork.util.ObjectFileUtil;
import com.bigbigwork.vo.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logout(){

    }
}
