package com.itheima;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Set;

public class ChatServerThread extends Thread {
    private Socket socket;

    public ChatServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 1.从socket中去获取当前客户端的输入流
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                // 读取客户端发送过来的指令
                String flag = br.readLine();
                System.out.println(flag);
                // 对客户端的指令进行处理
                switch (flag) { // 100: 表示登陆
                    case "100":
                        doLogin(br);
                        break;
                    case "200": // 200: 表示单聊
                        doSingleChat(br);
                        break;
                    case "201": // 201: 表示群聊
                        doAllChat(br);
                        break;

                    case "300": // 300: 客户端获取已上线的所有用户
                        doAllOnlineUser(br);
                        break;
                }
            }
        } catch (Exception e) {
            try {
                System.out.println("当前有人下线了！" + e.getMessage());
                // 从在线socket集合中移除本socket
                ChatServer.allSocketOnLine.remove(socket);
                // 发送在线用户给所有人
                String values = ChatServer.allSocketOnLine.values().toString();
                sendMsgToAllClient("有用户下线了，当前在线用户： " + values);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    // 获取已上线的所有用户
    private void doAllOnlineUser(BufferedReader br) throws Exception {
        // 得到已上线的所有用户
        String users = ChatServer.allSocketOnLine.values().toString();

        // 得到客户端对应的用户名
        User user = ChatServer.allSocketOnLine.get(socket);
        String userName = user.getUserName();

        // 发给请求的那个客户端
        sendMsgToOneClient(userName, "当前所有在线用户:" + users);
    }

    // 群聊
    private void doAllChat(BufferedReader br) throws Exception {
        // 群发消息
        String msg = br.readLine();
        // 群发消息给所有人
        sendMsgToAllClient(ChatServer.allSocketOnLine.get(socket).getUserName() + "对所有人说： " + msg);
    }

    // 单聊
    private void doSingleChat(BufferedReader br) throws Exception {
        // 私发消息
        // 获取对谁私发！
        String destName = br.readLine();
        // 获取私发消息
        String privateMsg = br.readLine();
        sendMsgToOneClient(destName, ChatServer.allSocketOnLine.get(socket).getUserName() + "对你说： " + privateMsg);
    }

    // 登陆
    private void doLogin(BufferedReader br) throws Exception {
        // 登录消息: 用户登录发送过来的数据格式为:用户名,性别,年龄
        String userInfo = br.readLine();
        String[] split = userInfo.split(",");
        String userName = split[0];
        String sex = split[1];
        int age = Integer.parseInt(split[2]);

        User user = new User(userName, sex, age);

        ChatServer.allSocketOnLine.put(socket, user);
        // 发送在线用户给所有人
        String values = userName + "用户登录了, 当前在线：" + ChatServer.allSocketOnLine.values().toString();
        sendMsgToAllClient(values);
    }

    /**
     *
     * @param destName 单聊的用户
     * @param privateMsg 单聊的消息
     */
    private void sendMsgToOneClient(String destName, String privateMsg) throws Exception {
        Set<Socket> keySet = ChatServer.allSocketOnLine.keySet();
        for (Socket sk : keySet) {
            User user = ChatServer.allSocketOnLine.get(sk);
            if (user.getUserName().equals(destName)) {
                PrintStream ps = new PrintStream(sk.getOutputStream());
                ps.println(privateMsg);
                ps.flush();
                break;
            }
        }
    }

    /**
     * 把当前客户端发来的消息推送给全部在线的socket
     *
     * @param msg
     */
    private void sendMsgToAllClient(String msg) throws Exception {
        Set<Socket> keySet = ChatServer.allSocketOnLine.keySet();
        for (Socket sk : keySet) {
            PrintStream ps = new PrintStream(sk.getOutputStream());
            ps.println(msg);
            ps.flush();
        }
    }
}
