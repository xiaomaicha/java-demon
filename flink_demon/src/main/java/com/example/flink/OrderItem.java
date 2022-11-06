package com.example.flink;
/*
 * @Classname OrderItem
 * @Description
 * @Date 2022/9/7 23:44
 * @author by dell
 */


import java.util.Date;


public class OrderItem {

    private String goodsId;
    private int count;

    private Date date;

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
