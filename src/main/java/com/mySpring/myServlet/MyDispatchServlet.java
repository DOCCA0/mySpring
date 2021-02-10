package com.mySpring.myServlet;

import com.myDemo.Student;
import com.mySpring.myAnnotations.MyAutowired;
import com.mySpring.myAnnotations.MyController;
import com.mySpring.myAnnotations.MyService;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
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
        //1,load the properties file
        doLoadConfig("application.properties");
        //2,scan the package,for example:com.myDemo
        doScanner(properties.getProperty("scan-package"));
        //3,put beans into ioc container
        doInstance();
        doAutowired();
    }




    /**
     * 1,load properties file into RAM,and we use MyDispatchServlet.properties to store them
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

    /**
     * 2,scan all the ".java" file in dir "path"
     * @param path package path
     */
    private void doScanner(String path) {
        File dirPath = new File("src/main/java/"+path.replace('.','/'));
        if (dirPath==null)
            return;
        for (File file:dirPath.listFiles()){
            if (file.isDirectory()){
                doScanner(file.getPath().replace("src/main/java/",""));
            }else{
                if (file.getAbsolutePath().endsWith(".java")){
                    String beanName=file.getPath().replace("src/main/java/","")
                            .replace("/",".").replace(".java","");
                    beanNameList.add(beanName);
                    log.info("beanListName add:"+beanName);
                }
            }
        }
    }
    //4、将iocMap里的类依赖注入

    /**
     * 3,put beans into ioc container
     */
    private void doInstance()  {
        for (String beanName:beanNameList){
            Class<?> aClass = null;
            try {
                aClass = Class.forName(beanName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (aClass.isAnnotationPresent(MyController.class)){
                Object o = null;
                try {
                    o = aClass.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                log.info("put "+beanName+" into container");
                iocMap.put(beanName,o);
            }else if(aClass.isAnnotationPresent(MyService.class)){


            }

        }
    }

    private void doAutowired() {
        for (Map.Entry<String,Object> entry: iocMap.entrySet()){
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field:fields){
                if(!field.isAnnotationPresent(MyAutowired.class)){
                    continue;
                }

            }
        }
    }
}
