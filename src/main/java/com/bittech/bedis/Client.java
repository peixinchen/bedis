package com.bittech.bedis;

import com.bittech.bedis.commands.CommandFactory;
import com.bittech.bedis.commands.ICommand;
import com.bittech.bedis.exceptions.BedisException;
import com.bittech.bedis.io.BedisInputStream;
import com.bittech.bedis.io.BedisOutputStream;
import com.bittech.bedis.protocol.Protocol;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        String host = "127.0.0.1"; //args[0];
        int port = 9988;    //Integer.parseInt(args[1]);

        Socket socket = new Socket(host, port);
        BedisInputStream is = new BedisInputStream(socket.getInputStream());
        BedisOutputStream os = new BedisOutputStream(socket.getOutputStream());

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("bedis> ");
            String cmdName = scanner.next();
            if (cmdName.equals("quit")) {
                break;
            }
            ICommand command = CommandFactory.buildFromClient(cmdName);
            command.readArgs(scanner);
            Protocol.writeCommand(os, command);
            os.flush();
            Object o = Protocol.read(is);
            if (o instanceof BedisException) {
                throw (BedisException)o;
            }

            Long len = (Long)o;

            System.out.printf("(integer) %d%n", len);
        }
    }
}
