package com.itheima;

import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
    实现客户端的开发
    客户端有两个功能:
        1.客户端可以收到服务端发送的各种消息,并打印
        2.客户端发送各种消息给服务端

    客户端和服务端消息通信规则约定(简称通信码):
         100: 表示登陆
         101: 表示注册

         200: 表示单聊
         201: 表示群聊
         202: 表示随机聊

         300: 客户端获取已上线的所有用户
         301: 客户端获取上线用户根据用户名升序排序
         302: 客户端获取指定性别的上线用户

         举例:客户端给服务端发送100,表示客户端要进行登录操作
 */
public class ChatClient {
    public static void main(String[] args) {
        try {

            // 1.创建于服务端的Socket,并保存到成员变量
            Socket socket = new Socket("127.0.0.1", 9999);
            System.out.println("连接服务器成功!");

            // 2.分配一个线程为客户端socket服务接收服务端发来的消息
            new ChatClientReaderThread(socket).start();

            // 获取Socket的输出流转成打印流,方便一次打印一行数据
            PrintStream ps = new PrintStream(socket.getOutputStream());

            Scanner sc = new Scanner(System.in);
            // 用户可以循环操作
            while (true) {
                // 3.打印操作信息
                System.out.println("1.登陆");
                System.out.println("2.注册");
                System.out.println("3.单聊");
                System.out.println("4.群聊");
                System.out.println("5.随机聊");
                System.out.println("6.获取上线用户根据用户名升序排序");
                System.out.println("7.获取指定性别的上线用户");
                System.out.println("8.获取已上线的所有用户");
                System.out.println("9.退出");
                System.out.println("请输入你的选择：");

                // 4.使用Scanner进行键盘输入
                String operation = sc.nextLine();
                System.out.println(operation);
                // 判断消息类型
                switch (operation) {
                    case "1": // 1.表示登陆
                        login(ps);
                        break;
                    case "2": // 2.表示注册
                        break;
                    case "3": // 3.表示单聊
                        sendOne(ps);
                        break;
                    case "4": // 4.表示群聊
                        sendAll(ps);
                        break;
                    case "5": // 5.表示随机聊
                        break;
                    case "6": // 6.获取上线用户根据用户名升序排序
                        break;
                    case "7": // 7.获取指定性别的上线用户
                        break;
                    case "8": // 8.获取已上线的所有用户
                        getAllOnlineUser(ps);
                        break;
                    case "9": // 9.退出
                        break;
                    default:
                        System.out.println("没有这样的操作!");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取所有已上线的用户
    private static void getAllOnlineUser(PrintStream ps) {
        System.out.println("进入获取所有已上线的用户");
        // 300: 客户端获取已上线的所有用户
        ps.println("300"); // 发送指令
        ps.flush();
    }


    // 单聊
    private static void sendOne(PrintStream ps) {
        System.out.println("进入单聊");
        Scanner sc = new Scanner(System.in);

        System.out.println("请输入对方姓名：");
        String destName = sc.nextLine();

        System.out.println("请输入消息：");
        String msg = sc.nextLine();

        // 200: 表示单聊
        ps.println("200");
        ps.println(destName);
        ps.println(msg);
        ps.flush();
    }

    // 群聊
    private static void sendAll(PrintStream ps) {
        System.out.println("进入群聊");
        Scanner sc = new Scanner(System.in);

        System.out.println("请输入群发信息：");
        String msg = sc.nextLine();

        // 201: 表示群聊
        ps.println("201");
        ps.println(msg);
        ps.flush();
    }

    public static void login(PrintStream ps) {
        Scanner sc = new Scanner(System.in);

        System.out.println("请输入登录名称：");
        String name = sc.nextLine();
        System.out.println("请输入登录性别：");
        String sex = sc.nextLine();
        System.out.println("请输入登录年龄：");
        String age = sc.next();

        // 100: 表示登陆
        ps.println("100");
        ps.println(name + "," + sex + "," + age);
        ps.flush();
    }
}
