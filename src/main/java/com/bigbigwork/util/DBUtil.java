package com.bigbigwork.util;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
/*
 * Utility class which is responsible to get JDBC connection object using
 * C3P0 DataSource connection pool With MYSQL Database.
 */
public class DBUtil {
    private static final String DB_USERNAME="db.username";
    private static final String DB_PASSWORD="db.password";
    private static final String DB_URL ="db.url";
    private static final String DB_DRIVER_CLASS="driver.class.name";

    private static Properties properties = null;
    private static ComboPooledDataSource dataSource;
    static{
        try {
            properties = new Properties();
            File file = new File("src/main/resources/database.properties");

            if(file.exists()){
                properties.load(new FileInputStream(file));
            }
            else{
                file = new File("database.properties");
                if(file.exists()){
                    properties.load(new FileInputStream(file));
                }else{
                    properties.load(DBUtil.class.getResourceAsStream("/database.properties"));
                }
            }

            dataSource = new ComboPooledDataSource();
            dataSource.setDriverClass(properties.getProperty(DB_DRIVER_CLASS));

            dataSource.setJdbcUrl(properties.getProperty(DB_URL));
            dataSource.setUser(properties.getProperty(DB_USERNAME));
            dataSource.setPassword(properties.getProperty(DB_PASSWORD));

            dataSource.setMinPoolSize(3);
            dataSource.setMaxPoolSize(10);
            dataSource.setAcquireIncrement(5);

        } catch (IOException | PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public static ComboPooledDataSource getDataSource(){
        return dataSource;
    }
}