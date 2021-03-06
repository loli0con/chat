package com.itheima;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatClientReaderThread extends Thread {
    private Socket socket;

    public ChatClientReaderThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedWriter out = new BufferedWriter(new FileWriter("client_log.txt", true))) {
            // 得到socket的字节输入流,包装成字符流,包装成字符输入缓冲流,方便后续以行为单位读取
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
            while ((msg = br.readLine()) != null) {
                System.out.println("你收到消息: " + msg);
                out.write(msg + "\n");
            }
            socket.shutdownInput();
            if (socket.isOutputShutdown()) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
