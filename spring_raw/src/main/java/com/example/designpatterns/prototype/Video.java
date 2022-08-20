package com.example.designpatterns.prototype;

import java.util.Date;

/**
 * @Classname Video
 * @Description TODO
 * @Date 2021/4/11 16:52
 * @Created by dell
 */
public class Video  implements Cloneable{
    private String name;
    private Date createTime;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void main(String[] args) {
        Date date = new Date();
        Date date1 = (Date) date.clone();

    }
}
