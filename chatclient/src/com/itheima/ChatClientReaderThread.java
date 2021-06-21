package com.itheima;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatClientReaderThread extends Thread {
    private Socket socket;

    public ChatClientReaderThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 得到socket的字节输入流,包装成字符流,包装成字符输入缓冲流,方便后续以行为单位读取
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
            while ((msg = br.readLine()) != null) {
                System.out.println("你收到消息: " + msg);
            }
            socket.shutdownInput();
            if (!socket.isOutputShutdown()) {
                socket.getOutputStream().flush();
                socket.shutdownOutput();
            }
            socket.close();
            System.out.println("客户端已关闭");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
