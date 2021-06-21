package com.itheima;

import java.net.Socket;


public class ChatClient {
    private static Socket socket;

    public static void main(String[] args) {
        try {
            // 1.创建于服务端的Socket,并保存到成员变量
            socket = new Socket("127.0.0.1", 9999);
            System.out.println("连接服务器成功!");

            CommandReader commandReader = new CommandReader();
            commandReader.setDaemon(true);
            commandReader.setSocket(socket);

            // 2.分配一个线程为客户端socket服务接收服务端发来的消息
            ChatClientReaderThread chatClientReaderThread = new ChatClientReaderThread(socket);

            commandReader.start();
            chatClientReaderThread.start();

            chatClientReaderThread.join();
            if (!commandReader.isInterrupted()) {
                commandReader.interrupt();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
