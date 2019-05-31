package com.bittech.bedis.commands;

import com.bittech.bedis.database.BedisDatabase;
import com.bittech.bedis.exceptions.BedisException;
import com.bittech.bedis.io.BedisOutputStream;
import com.bittech.bedis.protocol.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LLENCommand extends AbstractCommand {
    public LLENCommand() {
        super();
    }

    public LLENCommand(List<Object> args) {
        super(args);
    }

    @Override
    public void readArgs(Scanner scanner) {
        args = new ArrayList<>(1);
        args.add(scanner.next());
    }

    @Override
    public List<Object> getSerializedArgs() {
        List<Object> list = new ArrayList<>(1);
        String key = (String)args.get(0);
        list.add(key.getBytes(Protocol.getCharset()));

        return list;
    }

    @Override
    protected void processInner(BedisOutputStream os) throws BedisException {
        if (args.size() != 1) {
            throw new BedisException("LLEN 需要一个参数");
        }

        Object object = args.get(0);
        if (!(object instanceof byte[])) {
            throw new BedisException("参数必须是 bulk string 类型");
        }

        String key = new String((byte[])object, Protocol.getCharset());
        int len = 0;
        List<String> list = BedisDatabase.getList(key);
        if (list != null) {
            len = list.size();
        }

        Protocol.writeLong(os, len);
    }
}
