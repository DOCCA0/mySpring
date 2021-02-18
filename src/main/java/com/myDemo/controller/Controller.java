package com.myDemo.controller;

import com.myDemo.service.IService;
import com.mySpring.myAnnotations.MyAutowired;
import com.mySpring.myAnnotations.MyController;
import com.mySpring.myAnnotations.MyRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@MyController
@MyRequestMapping("/wu")
public class Controller {
    @MyAutowired
    private IService iService;

    @MyRequestMapping("/query")
    public void querySql(HttpServletRequest req, HttpServletResponse resp){
        String str = iService.querySql();
        try {
            resp.getWriter().write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
