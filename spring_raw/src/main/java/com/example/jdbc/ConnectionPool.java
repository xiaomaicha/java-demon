package com.example.jdbc;

/**
 * @description:
 * @author: WuQi
 * @time: 2020/7/12 22:41
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {

    List<Connection> cs = new ArrayList<Connection>();

    int size;

    public ConnectionPool(int size) {
        this.size = size;
        init();
    }

    public void init() {

        //这里恰恰不能使用try-with-resource的方式，因为这些连接都需要是"活"的，不要被自动关闭了
        try {
            Class.forName("com.mysql.jdbc.Driver");
            for (int i = 0; i < size; i++) {
                Connection c = DriverManager
                        .getConnection("javase.jdbc:mysql://127.0.0.1:3306/how2java?characterEncoding=UTF-8", "root", "wuqi");

                cs.add(c);

            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized Connection getConnection() {
        while (cs.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        Connection c = cs.remove(0);
        return c;
    }

    public synchronized void returnConnection(Connection c) {
        cs.add(c);
        this.notifyAll();
    }

}
