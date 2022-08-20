package com.example.javase.object;

/**
 * 描述 : 测试java中数据的传递
 *
 *
 * @Author : zhanghao
 * @Time 　: 2019/12/19 18:31
 */
public class ParamRef {

    public static void main(String[] args) {
        ParamRef ParamRef=new ParamRef();

        //1、测试基本数据类型
        int basicVal=1;
        System.out.println("参数前 basicVal:"+basicVal);
        ParamRef.basicDataTypeParam(basicVal);
        System.out.println("参数后 basicVal:"+basicVal);

        //2、测试引用类型参数
        Student stu=new Student("张三");
        System.out.println("参数前 stu.name:"+stu.getName());
        ParamRef.referDataTypeParam(stu);
        System.out.println("参数后 stu.name:"+stu.getName());

        //3、测试引用数组
        Integer[] arrInt=new Integer[10];
        arrInt[0]=123;
        System.out.println("参数前 arrInt.length:"+arrInt.length);
        System.out.println("参数前 arrInt[0]:"+arrInt[0]);
        ParamRef.referArrayParam(arrInt);
        System.out.println("参数后 arrInt.length:"+arrInt.length);
        System.out.println("参数后 arrInt[0]:"+arrInt[0]);

        //4、其它常用引用类型 如字符串 String
        String strVal="2008年，北京欢迎你";
        System.out.println("参数前 strVal:"+strVal);
        ParamRef.referStrParam(strVal);
        System.out.println("参数后 strVal:"+strVal);


    }

    /**
     * 测试字符串引用类型
     *
     *
     * @param strVal
     */
    private void referStrParam(String strVal) {
        strVal="2020年,北京欢迎你";
    }

    /**
     * 测试引用数组 同时改变参数值，并为参数重新赋值
     *
     * @param arrInt
     */
    private void referArrayParam(Integer[] arrInt) {
        arrInt[0]=456;
        arrInt=new Integer[20];
    }

    /**
     * 测试引用数据类型
     *
     * @param stu
     */
    private void referDataTypeParam(Student stu) {
        stu.setName("李四");
    }

    /**
     * 测试基本数据类型参数
     *
     * @param basicVal
     */
    private void basicDataTypeParam(int basicVal) {
        basicVal=2;
    }

    private static class Student {
        private String name;

        public Student() {
        }

        public Student(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    }