package com.myDemo.service.impl;

import com.myDemo.service.IService;
import com.mySpring.myAnnotations.MyService;

@MyService
public class ServiceImpl implements IService {
    public String querySql() {
        //pretend query mysql
        return "hello WU!";
    }
}
