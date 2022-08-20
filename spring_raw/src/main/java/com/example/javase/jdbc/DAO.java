package com.example.javase.jdbc;

import java.util.List;

import javase.charactor.Hero;

public interface DAO {
    //增加
    void add(Hero hero);

    //修改
    void update(Hero hero);

    //删除
    void delete(int id);

    //获取
    Hero get(int id);

    //查询
    List<Hero> list();

    //分页查询
    List<Hero> list(int start, int count);
}