package com.example.javase.innerclass;

public class Outer {
    public class Inner{
        public void print(){
            System.out.println("inner");
        }
    }

    public Inner getInner(){
        return new Inner();
    }

    public static void main(String[] args) {
        Inner inner = new Outer().new Inner();
    }
}
