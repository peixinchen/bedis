package com.bittech.bedis;

import com.bittech.bedis.commands.ICommand;
import com.bittech.bedis.io.BedisInputStream;
import com.bittech.bedis.io.BedisOutputStream;
import com.bittech.bedis.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static Logger logger = LoggerFactory.getLogger("bedis");

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9988);
        logger.info("服务器已经监听。");
        while (true) {
            Socket clientSocket = serverSocket.accept();
            logger.info("客户端已经连接: " + clientSocket.getInetAddress());
            BedisInputStream is = new BedisInputStream(clientSocket.getInputStream());
            BedisOutputStream os = new BedisOutputStream(clientSocket.getOutputStream());

            while (true) {
                ICommand command = Protocol.readCommand(is);
                logger.info("运行命令: " + command.getName());
                command.process(os);
                os.flush();
            }
        }
    }
}
