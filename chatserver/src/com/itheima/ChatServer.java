package com.itheima;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    // serverSocket
    private static ServerSocket ss;


    public static void main(String[] args) {

        loadUsers();
        int millisecond = 2000;

        try {
            ss = new ServerSocket(9999);
            ss.setSoTimeout(millisecond);

            System.out.println("服务端已启动!");
            addShutdownHook(Thread.currentThread());

            while (true) {
                Socket socket = null;
                try {
                    socket = ss.accept();
                } catch (SocketTimeoutException e) {
                    System.out.println(millisecond / 1000.0 + "秒内无客户端连接到此");
                    if (Thread.currentThread().isInterrupted()) {
                        ss.close();
                        break;
                    }
                }
                // 为当前登录成功的socket分配一个独立的线程来处理与之通信
                if (!Objects.isNull(socket)) {
                    new ChatServerThread(socket).start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 加载users集合
     */
    private static void loadUsers() {
        if (!Files.exists((Paths.get("user.txt")))) {
            System.out.println("找不到user.txt文件, 无初始化");
            return;
        }
        Object obj;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("user.txt"))) {
            while (ois.available() > 0) {
                obj = ois.readObject();
                users.add((User) obj);
            }
            System.out.println("已根据user.txt文件, 完成初始化流程");
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
            System.out.println("已保存用户信息到user.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addShutdownHook(Thread main_thread) throws InterruptedException {
        // 程序结束时
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            main_thread.interrupt();
            while (ss.isClosed()) ;
            saveUsers();
            for (Socket socket : allSocketOnLine.keySet()) {
                try {
                    socket.getOutputStream().flush();
                    socket.shutdownOutput();
                    if (!socket.isInputShutdown()) {
                        socket.shutdownInput();
                    }
                    if (!socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("释放所有资源");
        }));
    }
}
