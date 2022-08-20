package com.example.javase.io;

//import java.javase.io.*;
//
///**
// * @description:
// * @author: WuQi
// * @time: 2020/2/6 12:23
// */
//
//public class Serialize {
//    public static void main(String[] args) throws IOException, ClassNotFoundException {
//
//        A a1 = new A(123, "abc");
//        String objectFile = "file_a1.txt";
//
//        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(objectFile));
//        objectOutputStream.writeObject(a1);
//        objectOutputStream.close();
//
//        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(objectFile));
//        A a2 = (A) objectInputStream.readObject();
//        objectInputStream.close();
//        System.out.println(a2);
//    }
//
//    private static class A implements Serializable {
//
//        private int x;
//        private String y;
//
//        A(int x, String y) {
//            this.x = x;
//            this.y = y;
//        }
//
//        @Override
//        public String toString() {
//            return "x = " + x + "  " + "y = " + y;
//        }
//    }
//}

import java.io.*;

public class Serialize {
    public static void main(String[] args) {
        ObjectOutputStream osc = null;
        ObjectInputStream osr = null;
        Person ss = new Person("悟空", 007, 95.5f);
        Person ss2 = null;
        try {
            FileOutputStream wsc = new FileOutputStream("file_a1.dat"); //把数据写到文件里
            osc = new ObjectOutputStream(wsc);
            osc.writeObject(ss);
            osr = new ObjectInputStream(new FileInputStream("file_a1.dat"));//从文件中读取数据
            ss2 = (Person) osr.readObject();
//readObject()方法就是用于读取数据，并且要进行类型转换
            System.out.println("姓名：" + ss2.xingming);
            System.out.println("学号：" + ss2.xuehao);
            System.out.println("成绩：" + ss2.chengji);
        } catch (Exception e) {
            System.out.println("出现错误");
            e.printStackTrace();
        } finally //必须执行，作用是善后;如果try…catch下面有finally，那么cry…catch中不要写退出程序的代码
        {
            try {
                osc.close();
                osr.close();
                System.exit(-1); //退出程序
            } catch (Exception e) {
                System.exit(-1);
            }
        }
    }
}

class Person implements Serializable //实现serializable可序列化接口的作用是就是可以把对象存到字节流，然后可以恢复;
{
    String xingming = null;
    int xuehao = 0;
    float chengji = 0;

    // transient float chengji = 0; //transient作用是忽略，外界传不进来数据
    public Person(String xingming, int xuehao, float chengji) {
        this.xingming = xingming;
        this.xuehao = xuehao;
        this.chengji = chengji;
    }
}

