package com.example.jdbc;

/*
 * @description:
 * @author: WuQi
 * @time: 2020/7/12 17:00
 */

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestJDBC {
    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (
                Connection c = DriverManager.getConnection("javase.jdbc:mysql://127.0.0.1:3306/how2java?characterEncoding=UTF-8",
                        "root", "wuqi");
                Statement s = c.createStatement()
        ) {
            String sql = "insert into hero values(null," + "'提莫'" + "," + 313.0f + "," + 50 + ")";
            s.execute(sql);

            // 查看数据库层面的元数据
            // 即数据库服务器版本，驱动版本，都有哪些数据库等等

            DatabaseMetaData dbmd = c.getMetaData();

            // 获取数据库服务器产品名称
            System.out.println("数据库产品名称:\t" + dbmd.getDatabaseProductName());
            // 获取数据库服务器产品版本号
            System.out.println("数据库产品版本:\t" + dbmd.getDatabaseProductVersion());
            // 获取数据库服务器用作类别和表名之间的分隔符 如test.user
            System.out.println("数据库和表分隔符:\t" + dbmd.getCatalogSeparator());
            // 获取驱动版本
            System.out.println("驱动版本:\t" + dbmd.getDriverVersion());

            System.out.println("可用的数据库列表：");
            // 获取数据库名称
            ResultSet rs = dbmd.getCatalogs();

            while (rs.next()) {
                System.out.println("数据库名称:\t" + rs.getString(1));
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}