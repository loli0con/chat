package com.itheima;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * 实现客户端的开发
 * 客户端有两个功能:
 * 1.客户端可以收到服务端发送的各种消息,并打印
 * 2.客户端发送各种消息给服务端
 * <p>
 * 客户端和服务端消息通信规则约定(简称通信码):
 * 100: 表示登陆
 * 101: 表示注册
 * <p>
 * 200: 表示单聊
 * 201: 表示群聊
 * 202: 表示随机聊
 * <p>
 * 300: 客户端获取已上线的所有用户
 * 301: 客户端获取上线用户根据用户名升序排序
 * 302: 客户端获取指定性别的上线用户
 * <p>
 * 举例:客户端给服务端发送100,表示客户端要进行登录操作
 */
public class ChatClient {
    private static Socket socket;

    public static void main(String[] args) {
        try {
            // 1.创建于服务端的Socket,并保存到成员变量
            socket = new Socket("127.0.0.1", 9999);
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
                        register(ps);
                        break;
                    case "3": // 3.表示单聊
                        sendOne(ps);
                        break;
                    case "4": // 4.表示群聊
                        sendAll(ps);
                        break;
                    case "5": // 5.表示随机聊
                        sendSomeone(ps);
                        break;
                    case "6": // 6.获取上线用户根据用户名升序排序
                        getAllOnlineUserOrderByName(ps);
                        break;
                    case "7": // 7.获取指定性别的上线用户
                        getAllOnlineUserFilterBySex(ps);
                        break;
                    case "8": // 8.获取已上线的所有用户
                        getAllOnlineUser(ps);
                        break;
                    case "9": // 9.退出
                        closeClient();
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

    // 0.查看聊天记录
    public static void showHistory() {
        // TODO
    }

    // 1.表示登陆
    public static void login(PrintStream ps) {
        Scanner sc = new Scanner(System.in);

        System.out.println("请输入用户名：");
        String name = sc.nextLine();
        System.out.println("请输入密码：");
        String password = sc.nextLine();

        // 100: 表示登陆
        ps.println("100");
        ps.println(name + "," + password);
        ps.flush();
    }

    // 2.表示注册
    public static void register(PrintStream ps) {
        Scanner sc = new Scanner(System.in);

        System.out.println("请输入注册的用户名称(首字母为a-z或A-Z,其它部分可以为字母、数字或者下划线，长度在10个字符以内)：");
        String name = sc.nextLine();
        while (true) {
            if (name.matches("^[a-zA-Z][a-zA-Z0-9_]{0,9}$")) {
                break;
            }
            System.out.println("您输入的用户名不符合要求,请重新输入:");
            name = sc.nextLine();
        }
        System.out.println("请输入登录性别：");
        String sex = sc.nextLine();
        System.out.println("请输入登录年龄：");
        String age = sc.nextLine();
        System.out.println("请输入密码(同用户名要求)：");
        String password = sc.nextLine();
        while (true) {
            if (password.matches("^[a-zA-Z][a-zA-Z0-9_]{0,9}$")) {
                break;
            }
            System.out.println("您输入的密码不符合要求,请重新输入!");
            password = sc.nextLine();
        }

        // 101: 表示注册
        ps.println("101");
        ps.println(name);
        ps.println(sex);
        ps.println(age);
        ps.println(password);
        ps.flush();
    }

    // 3.表示单聊
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

    // 4.表示群聊
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

    // 5.表示随机聊
    private static void sendSomeone(PrintStream ps) {
        System.out.println("进入单聊");
        Scanner sc = new Scanner(System.in);

        System.out.println("请输入消息：");
        String msg = sc.nextLine();

        // 201: 表示随机聊
        ps.println("202");
        ps.println(msg);
        ps.flush();
    }

    // 6.获取上线用户根据用户名升序排序
    private static void getAllOnlineUserOrderByName(PrintStream ps) {
        System.out.println("进入获取上线用户根据用户名升序排序");

        // 301: 客户端获取上线用户根据用户名升序排序
        ps.println("301"); // 发送指令
        ps.flush();
    }

    // 7.获取指定性别的上线用户
    private static void getAllOnlineUserFilterBySex(PrintStream ps) {
        System.out.println("进入获取指定性别的上线用户");
        Scanner sc = new Scanner(System.in);

        System.out.println("请输入性别：");
        String sex = sc.nextLine();

        // 302: 客户端获取指定性别的上线用户
        ps.println("302"); // 发送指令
        ps.println(sex);
        ps.flush();
    }

    // 8.获取已上线的所有用户
    private static void getAllOnlineUser(PrintStream ps) {
        System.out.println("进入获取所有已上线的用户");

        // 300: 客户端获取已上线的所有用户
        ps.println("300"); // 发送指令
        ps.flush();
    }

    // 9.退出
    private static void closeClient() throws IOException {
        socket.shutdownInput();
    }
}
