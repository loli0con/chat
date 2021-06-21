package com.itheima;

public class User {
    private String userName;
    private String sex;
    private int age;


    public User() {
    }

    public User(String userName, String sex, int age) {
        this.userName = userName;
        this.sex = sex;
        this.age = age;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String toString() {
        return "User{userName = " + userName + ", sex = " + sex + ", age = " + age + "}";
    }
}
