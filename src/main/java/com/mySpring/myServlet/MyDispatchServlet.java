package com.mySpring.myServlet;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * my servlet
 */
public class MyDispatchServlet extends HttpServlet {
    /**
     * log,not important
     */
    final  Logger log = Logger.getLogger(MyDispatchServlet. class);
    /**
     * to store the scan-package
     */
    private Properties properties=new Properties();
    /**
     * to store the beanName
     */
    private List<String> beanNameList=new ArrayList<String>();
    /**
     * as an ioc container
     */
    private Map<String,Object> iocMap=new HashMap<String, Object>();

    @Override
    public void init() throws ServletException {
        //1、load the properties file
        doLoadConfig("application.properties");
        doScanner(properties.getProperty("scan-package"));
    }



    /**
     * load properties file into RAM,and we use MyDispatchServlet.properties to store them
     * @param contextConfigLocation file path of the properties file
     */
    private void doLoadConfig(String contextConfigLocation) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            properties.load(resourceAsStream);
            log.info("load properties file success");
            log.info(String.format("properties file details：%s",properties.toString()));
        } catch (IOException e) {
            log.error("load properties file error");
            e.printStackTrace();
        }finally {
            if (null != resourceAsStream) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doScanner(String path) {
        System.out.printf(path);
    }
    //2、扫描相关类
    //3、将相关类保存在iocMap里
    //4、将iocMap里的类依赖注入
}
