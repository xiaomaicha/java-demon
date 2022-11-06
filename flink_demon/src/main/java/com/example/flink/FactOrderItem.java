package com.example.flink;/*
 * @Classname FactOrderItem
 * @Description
 * @Date 2022/9/7 23:46
 * @author by dell
 */


public class FactOrderItem {
    private String goodsId;
    private String goodsName;
    private int totalMoney;
    private int count;

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public int getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(int totalMoney) {
        this.totalMoney = totalMoney;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "FactOrderItem{" +
                "goodsId='" + goodsId + '\'' +
                ", goodsName='" + goodsName + '\'' +
                ", totalMoney=" + totalMoney +
                ", count=" + count +
                '}';
    }
}
