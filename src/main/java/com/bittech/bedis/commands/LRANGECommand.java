package com.bittech.bedis.commands;

import com.bittech.bedis.database.BedisDatabase;
import com.bittech.bedis.exceptions.BedisException;
import com.bittech.bedis.io.BedisOutputStream;
import com.bittech.bedis.protocol.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LRANGECommand extends AbstractCommand {
    public LRANGECommand() {
        super();
    }

    public LRANGECommand(List<Object> args) {
        super(args);
    }

    @Override
    public void readArgs(Scanner scanner) {
        args = new ArrayList<>(3);
        args.add(scanner.next());
        args.add(scanner.nextInt());
        args.add(scanner.nextInt());
    }

    @Override
    public List<Object> getSerializedArgs() {
        List<Object> list = new ArrayList<>();
        String s = (String)args.get(0);
        list.add(s.getBytes(Protocol.getCharset()));
        Integer start = (Integer)args.get(1);
        Integer stop = (Integer)args.get(2);
        list.add(Integer.toString(start).getBytes(Protocol.getCharset()));
        list.add(Integer.toString(stop).getBytes(Protocol.getCharset()));

        return list;
    }

    @Override
    protected void processInner(BedisOutputStream os) throws BedisException {
        if (args.size() != 3) {
            throw new BedisException("LRANGE key start stop");
        }

        Object object = args.get(0);
        if (!(object instanceof byte[])) {
            throw new BedisException("key 必须是 bulk string 类型");
        }

        String key = new String((byte[])object, Protocol.getCharset());
        List<String> list = BedisDatabase.getList(key);
        if (list == null) {
            Protocol.writeError(os, "空的");
            return;
        }

        byte[] startBytes = (byte[])args.get(1);
        byte[] stopBytes = (byte[])args.get(2);
        int start = Integer.parseInt(new String(startBytes, Protocol.getCharset()));
        int stop = Integer.parseInt(new String(stopBytes, Protocol.getCharset()));
        if (start < 0) {
            start = list.size() + start;
        }
        if (stop < 0) {
            stop = list.size() + stop;
        }
        logger.debug("lrange key list 长度: " + list.size());
        logger.debug("lrange start: " + start);
        logger.debug("lrange stop: " + stop);
        logger.debug("lrange 返回总长度: " + (stop - start + 1));
        List<Object> ret = new ArrayList<>(stop - start + 1);
        for (int i = start; i <= stop; i++) {
            ret.add(list.get(i));
        }

        Protocol.writeList(os, ret);
    }

    @Override
    protected void displayReplyInner(Object object) throws BedisException {
        List<Object> list = (List<Object>)object;
        for (int i = 0; i < list.size(); i++) {
            byte[] bytes = (byte[])list.get(i);
            String value = new String(bytes, Protocol.getCharset());
            System.out.printf("%d) %s%n", i + 1, value);
        }
    }
}
