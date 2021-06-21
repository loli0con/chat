package com.itheima;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.*;

public class ChatServerThread extends Thread {
    private final Socket socket;

    public ChatServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // 除了注册和登录以外的所有功能
        String[] signal_arr = {"200", "201", "202", "300", "301", "302"};
        try {
            // 1.从socket中去获取当前客户端的输入流
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                // 读取客户端发送过来的指令
                String flag = br.readLine();

                if (Objects.isNull(flag)) {
                    this.socket.shutdownInput();
                    this.socket.getOutputStream().flush();
                    this.socket.shutdownOutput();
                    this.socket.close();
                    ChatServer.allSocketOnLine.remove(this.socket);
                }

                // 拦截游客访问用户的功能
                if (!hasLogin() && Arrays.asList(signal_arr).contains(flag)) {
                    sendMsgToCurrentSocket("尚未登录，无法访问该功能");
                    continue;
                }

                // 对客户端的指令进行处理
                switch (flag) {
                    case "100": // 100: 表示登陆
                        doLogin(br);
                        break;
                    case "101": // 101: 表示注册
                        doRegister(br);
                        break;

                    case "200": // 200: 表示单聊
                        doSingleChat(br);
                        break;
                    case "201": // 201: 表示群聊
                        doAllChat(br);
                        break;
                    case "202": // 202: 表示随机聊
                        doRandomChat(br);
                        break;

                    case "300": // 300: 客户端获取已上线的所有用户
                        doAllOnlineUser(br);
                        break;
                    case "301": // 301: 客户端获取上线用户根据用户名升序排序
                        doAllOnlineUserOrderByUserName(br);
                        break;
                    case "302": // 302: 客户端获取指定性别的上线用户
                        doAllOnlineUserFilterBySex(br);
                        break;
                }
            }
        } catch (Exception e) {
            try {
                if (hasLogin()) {
                    System.out.println("当前有人下线了！" + e.getMessage());
                    // 从在线socket集合中移除本socket
                    ChatServer.allSocketOnLine.remove(socket);
                    // 发送在线用户给所有人
                    String values = ChatServer.allSocketOnLine.values().toString();
                    sendMsgToAllUser("有用户下线了，当前在线用户： " + values);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }


    // 100: 登陆
    private void doLogin(BufferedReader br) throws Exception {
        // 登录消息: 用户登录发送过来的数据格式为:用户名,密码
        String userInfo = br.readLine();
        String[] split = userInfo.split(",");
        String userName = split[0];
        String password = split[1];

        User targetUser = null;
        for (User user : ChatServer.users) {
            if (user.getUserName().equals(userName)) {
                targetUser = user;
                break;
            }
        }

        if (Objects.isNull(targetUser)) {
            sendMsgToCurrentSocket("该用户不存在, 请确认用户名是否正确");
        } else {  // 用户存在
            if (!targetUser.getPassword().equals(password)) {
                sendMsgToCurrentSocket("密码错误");
            } else { //密码正确
                ChatServer.allSocketOnLine.put(socket, targetUser);
                // 发送在线用户给所有人
                String values = userName + "用户登录了, 当前在线：" + ChatServer.allSocketOnLine.values().toString();
                sendMsgToAllUser(values);
            }
        }
    }

    // 101: 注册
    private void doRegister(BufferedReader br) throws Exception {
        String userName = br.readLine();
        String sex = br.readLine();
        int age = Integer.parseInt(br.readLine());
        String password = br.readLine();

        User new_user = new User(userName, sex, age, password);
        if (ChatServer.users.add(new_user)) {
            sendMsgToCurrentSocket("恭喜您:" + userName + ", 注册成功!");
        } else {
            sendMsgToCurrentSocket(userName + "用户已经存在，注册失败!");
        }
    }


    // 200: 单聊
    private void doSingleChat(BufferedReader br) throws Exception {
        // 私发消息
        // 获取对谁私发！
        String destName = br.readLine();
        // 获取私发消息
        String privateMsg = br.readLine();
        sendMsgToOneUser(destName,
                ChatServer.allSocketOnLine.get(socket).getUserName() + "对你说： " + privateMsg);
    }

    // 201: 群聊
    private void doAllChat(BufferedReader br) throws Exception {
        // 群发消息
        String msg = br.readLine();
        // 群发消息给所有人
        sendMsgToAllUser(ChatServer.allSocketOnLine.get(socket).getUserName() + "对所有人说： " + msg);
    }

    // 202: 随机聊
    private void doRandomChat(BufferedReader br) throws Exception {
        String privateMsg = br.readLine();
        Socket socket = ChatServer.allSocketOnLine.keySet().stream()
                .filter(sock -> sock == this.socket)
                .skip(new Random().nextInt(ChatServer.allSocketOnLine.size() - 1))
                .findFirst()
                .orElse(this.socket);
        String userName = ChatServer.allSocketOnLine.get(socket).getUserName();
        sendMsgToCurrentSocket("你随机聊的目标为" + userName);
        sendMsgToOneUser(userName, privateMsg);
    }


    // 300: 获取已上线的所有用户
    private void doAllOnlineUser(BufferedReader br) throws Exception {
        // 得到已上线的所有用户
        String users = ChatServer.allSocketOnLine.values().toString();

        // 得到客户端对应的用户名
        User user = ChatServer.allSocketOnLine.get(socket);
        String userName = user.getUserName();

        // 发给请求的那个客户端
        sendMsgToOneUser(userName, "当前所有在线用户:" + users);
    }

    // 301: 获取上线用户根据用户名升序排序
    private void doAllOnlineUserOrderByUserName(BufferedReader br) throws Exception {
        // 得到已上线的所有用户
        Collection<User> userCollection = ChatServer.allSocketOnLine.values();
        List<User> userList = new ArrayList<>(userCollection);
        // 排序
        userList.sort(Comparator.comparing(User::getUserName));
        String users = userList.toString();

        // 得到客户端对应的用户名
        User user = ChatServer.allSocketOnLine.get(socket);
        String userName = user.getUserName();

        // 发给请求的那个客户端
        sendMsgToOneUser(userName, "根据用户名排序的用户:" + users);
    }

    // 302: 客户端获取指定性别的上线用户
    private void doAllOnlineUserFilterBySex(BufferedReader br) throws Exception {
        // 得到目标性别
        String sex = br.readLine();
        // 得到已上线的所有用户
        Collection<User> userCollection = ChatServer.allSocketOnLine.values();
        List<User> userList = new ArrayList<>(userCollection);
        userList.removeIf(user -> !user.getSex().equals(sex));
        String users = userList.toString();

        // 得到客户端对应的用户名
        User user = ChatServer.allSocketOnLine.get(socket);
        String userName = user.getUserName();

        // 发给请求的那个客户端
        sendMsgToOneUser(userName, "已上线的" + sex + "性用户:" + users);
    }


    /**
     * 推送消息给当前游客
     *
     * @param privateMsg 单聊的消息
     */
    private void sendMsgToCurrentSocket(String privateMsg) throws Exception {
        PrintStream ps = new PrintStream(socket.getOutputStream());
        ps.println(privateMsg);
        ps.flush();
    }

    /**
     * @param destName   单聊的用户
     * @param privateMsg 单聊的消息
     */
    private void sendMsgToOneUser(String destName, String privateMsg) throws Exception {
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
    private void sendMsgToAllUser(String msg) throws Exception {
        Set<Socket> keySet = ChatServer.allSocketOnLine.keySet();
        for (Socket sk : keySet) {
            PrintStream ps = new PrintStream(sk.getOutputStream());
            ps.println(msg);
            ps.flush();
        }
    }

    /**
     * 判断当前socket是否已经登录，或者只是一个guest
     *
     * @return true——该socket已登录，即是用户；否则返回false。
     */
    private boolean hasLogin() {
        Set<Socket> keySet = ChatServer.allSocketOnLine.keySet();
        return keySet.contains(this.socket);
    }
}
