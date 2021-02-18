package com.mySpring.myServlet;

import com.mySpring.myAnnotations.MyAutowired;
import com.mySpring.myAnnotations.MyController;
import com.mySpring.myAnnotations.MyRequestMapping;
import com.mySpring.myAnnotations.MyService;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
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

    /**
     * for example:<"/index",getIndex()>
     */
    Map<String, Method> handlerMapping = new HashMap<String, Method>();
    @Override
    public void init() throws ServletException {
        //1,load the properties file
        doLoadConfig("application.properties");
        //2,scan the package,for example:com.myDemo
        doScanner(properties.getProperty("scan-package"));
        //3,put beans into ioc container
        doInstance();
        //4,get beans from iocMap and inject them
        doAutowired();
        //5,init handler,for example:("/index")
        initHandlerMapping();
    }



    /**
     * first char to lower
     * @param str
     * @return
     */
    private String toLowerFirstCase(String str) {
        char[] charArray = str.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
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
        // '/' means root path of the project instead of root path of the computer
        URL resourcePath = this.getClass().getClassLoader().getResource("/" + path.replaceAll("\\.", "/"));

        if (resourcePath == null) {
            return;
        }

        File classPath = new File(resourcePath.getFile());
        for (File file:classPath.listFiles()){
            if (file.isDirectory()){
                doScanner(path + "." + file.getName());
            }else{
                if (file.getName().endsWith(".class")) {
                    String beanName = (path + "." + file.getName()).replace(".class", "");
                    beanNameList.add(beanName);
                    log.info("beanListName add:"+beanName);
                }
            }
        }
    }

    /**
     * 3,put beans into ioc container
     */
    private void doInstance() {
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
//                int len=beanName.split("\\.").length;
//                if(len>=1){
//                    log.info("put "+beanName+" into container");
//                    iocMap.put(this.toLowerFirstCase(beanName.split("\\.")[len-1]) ,o);
//                }
                log.info("put "+beanName+" into container");
                iocMap.put(beanName,o);
            }else if(aClass.isAnnotationPresent(MyService.class)){
                Object o=null;
                try {
                    o = aClass.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                log.info("put "+beanName+" into container");
                iocMap.put(beanName,o);
                //put all the interface into iocMap
                for (Class i:aClass.getInterfaces()){
                    if(!iocMap.containsKey(i.getName())){
                        log.info("put "+i.getName()+" into container");
                        iocMap.put(i.getName(),o);
                    }
                }
            }
        }
        log.info("iocMap:"+iocMap.toString());
    }

    /**
     * 4,get beans from iocMap and inject them
     */
    private void doAutowired() {
        for (Map.Entry<String,Object> entry: iocMap.entrySet()){
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field:fields){
                if(!field.isAnnotationPresent(MyAutowired.class)){
                    continue;
                }
                //we can set it if the field is private
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(),iocMap.get(field.getGenericType().toString().split("\\s+")[1]));
                    log.info(entry.getValue().toString()+"  "+iocMap.get(field.getGenericType().toString().split("\\s+")[1]));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 5,init handler,for example:("/index")
     */
    private void initHandlerMapping() {
        for (Map.Entry<String,Object> entry:iocMap.entrySet()){
            Class<?> aClass = entry.getValue().getClass();
            if(!aClass.isAnnotationPresent(MyController.class)){
                continue;
            }
            String baseurl="";
            if(aClass.isAnnotationPresent(MyRequestMapping.class)){
                baseurl=aClass.getAnnotation(MyRequestMapping.class).value();
            }
            for (Method method:aClass.getMethods()){
                if (!method.isAnnotationPresent(MyRequestMapping.class)){
                    continue;
                }
                String url=("/"+baseurl+"/"+method.getAnnotation(MyRequestMapping.class).value()).
                        replaceAll("/+","/");
                handlerMapping.put(url,method);
                log.info(String.format("%s to %s",url,method.toString()));
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestURL = req.getRequestURI();
        String contextPath = req.getContextPath();
        String url = requestURL.replaceAll(contextPath, "").replaceAll("/+", "/");
        log.info("request url:"+url+";request path:"+contextPath);
        if(!handlerMapping.containsKey(url)){
            resp.getWriter().write("404 NOT FOUND");
        }
        Method method = handlerMapping.get(url);
        try {
            method.invoke(iocMap.get(toLowerFirstCase(method.getDeclaringClass().getSimpleName())),req,resp);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
