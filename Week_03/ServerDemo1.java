package com.citiccard.nio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerDemo1 {

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(40);
        ServerSocket serverSocket = new ServerSocket(8082);
        while(true) {
            Socket socket = serverSocket.accept();
            executorService.submit(()->{
                service(socket);
            });
        }
    }
    public static void service(Socket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int read = 0;
            while((read = inputStream.read(buffer))!= -1) {
                for(int i = 0; i<read; i++) {
                    System.out.print((char)buffer[i]);
                }
             }

            Thread.sleep(20);
            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os, true);
            pw.println("HTTP/1.1 200 OK");
            pw.println("Content-Type:text/html;charset=UTF-8");
            String body = "hello, nio";
            pw.println("Content-Length:" + body.getBytes().length);
            pw.println();
            pw.write(body);
            pw.close();
            socket.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
