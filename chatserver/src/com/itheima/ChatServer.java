package com.itheima;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 服务端实现的需求：
 * 1.创建服务端，绑定端口
 * 2.接收客户端的socket连接，交给一个独立的线程来处理
 * 3.把当前连接的客户端socket存入到一在线socket集合中保存
 * 4.接收客户端的消息，然后推送给当前所有在线的socket接收
 */
public class ChatServer {
    // 存放登录信息, 键是Socket,值用户信息
    public static Map<Socket, User> allSocketOnLine = new ConcurrentHashMap<>();

    // 存放用户信息
    public static Set<User> users = new CopyOnWriteArraySet<>();

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

    /**
     * 加载users集合
     */
    private static void loadUsers() {
        Object obj;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("user.txt"))) {
            while (true) {
                obj = ois.readObject();
                if (Objects.isNull(obj)) {
                    break;
                }
                users.add((User) obj);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存users集合
     */
    private static void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("user.txt"))) {
            for (User user : users) {
                oos.writeObject(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
