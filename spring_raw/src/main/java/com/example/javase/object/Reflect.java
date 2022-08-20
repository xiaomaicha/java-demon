package com.example.javase.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class Reflect {
    private int age;
    private String name;
    private int testing;

    public Reflect(int age) {
        this.age = age;
    }

    public Reflect(int age, String name) {
        this.age = age;
        this.name = name;
    }

    private Reflect(String name) {
        this.name = name;
    }

    public Reflect() {
    }

    public static void main(String[] args) {
        Reflect reflect = new Reflect();
        Class c4 = reflect.getClass();
        Constructor[] constructors;
        constructors = c4.getDeclaredConstructors();

        for (int i = 0; i < constructors.length; i++) {
            System.out.println(Modifier.toString((constructors[i].getModifiers())) + "参数");
            Class[] parametertypes = constructors[i].getParameterTypes();
            for (int j = 0; j < parametertypes.length; j++) {
                System.out.println(parametertypes[j].getName() + " ");
            }
            System.out.println();
        }
    }
}
