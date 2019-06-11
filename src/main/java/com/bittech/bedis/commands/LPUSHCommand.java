package com.bittech.bedis.commands;

import com.bittech.bedis.database.BedisDatabase;
import com.bittech.bedis.exceptions.BedisException;
import com.bittech.bedis.io.BedisOutputStream;
import com.bittech.bedis.protocol.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LPUSHCommand extends AbstractCommand {
    public LPUSHCommand() {
        super();
    }

    public LPUSHCommand(List<Object> args) {
        super(args);
    }

    @Override
    public void readArgs(Scanner scanner) {
        args = new ArrayList<>(1);
        while (scanner.hasNext()) {
            args.add(scanner.next());
        }
        logger.debug("输入全部读完");
    }

    @Override
    public List<Object> getSerializedArgs() {
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < args.size(); i++) {
            String s = (String)args.get(0);
            list.add(s.getBytes(Protocol.getCharset()));
        }

        return list;
    }

    @Override
    protected void processInner(BedisOutputStream os) throws BedisException {
        if (args.size() < 2) {
            throw new BedisException("LPUSH 至少需要 key 和 一个 value");
        }

        Object object = args.get(0);
        if (!(object instanceof byte[])) {
            throw new BedisException("key 必须是 bulk string 类型");
        }

        String key = new String((byte[])object, Protocol.getCharset());
        List<String> list = BedisDatabase.getListOrCreate(key);
        for (int i = 1; i < args.size(); i++) {
            Object vobj = args.get(i);
            if (!(vobj instanceof byte[])) {
                throw new BedisException("value 必须是 bulk string 类型");
            }
            byte[] value = (byte[])vobj;
            synchronized (list) {
                list.add(new String(value, Protocol.getCharset()));
            }
        }
        int len = 0;
        synchronized (list) {
            len = list.size();
        }

        Protocol.writeLong(os, len);
    }

    @Override
    protected void displayReplyInner(Object object) throws BedisException {
        Long len = (Long)object;
        System.out.println("(integer) " + len);
    }
}
