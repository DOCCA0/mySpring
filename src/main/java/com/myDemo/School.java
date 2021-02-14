package com.myDemo;


import com.mySpring.myAnnotations.MyAutowired;
import com.mySpring.myAnnotations.MyController;

@MyController
public class School {
    @MyAutowired
    private Student student;

    @Override
    public String toString() {
        return "School{" +
                "student=" + student +
                '}';
    }
}
