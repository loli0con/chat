package com.itheima;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
    服务端实现的需求：
        1.创建服务端，绑定端口
        2.接收客户端的socket连接，交给一个独立的线程来处理
        3.把当前连接的客户端socket存入到一在线socket集合中保存
        4.接收客户端的消息，然后推送给当前所有在线的socket接收
 */
public class ChatServer {
    // 存放登录信息, 键是Socket,值用户信息
    public static Map<Socket, User> allSocketOnLine = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(9999);
            System.out.println("服务端已启动!");
            while (true) {
                Socket socket = ss.accept();
                // 为当前登录成功的socket分配一个独立的线程来处理与之通信
                new ChatServerThread(socket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
