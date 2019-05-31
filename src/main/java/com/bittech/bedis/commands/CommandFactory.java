package com.bittech.bedis.commands;

import com.bittech.bedis.exceptions.BedisException;
import com.bittech.bedis.protocol.Protocol;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public final class CommandFactory {
    private CommandFactory() {}

    public static ICommand buildFromClient(String name) throws BedisException {
        String className = "com.bittech.bedis.commands." + name.toUpperCase() + "Command";
        try {
            Class<?> cls = Class.forName(className);
            return (ICommand)cls.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new BedisException(e);
        }
    }

    public static ICommand buildFromServer(List<Object> args) throws BedisException {
        if (args.size() == 0) {
            throw new BedisException("都是空的");
        }

        if (!(args.get(0) instanceof byte[])) {
            throw new BedisException("收到的命令应该是字节流");
        }

        String cmdName = new String((byte[])args.get(0), Protocol.getCharset())
                .toUpperCase();
        String className = "com.bittech.bedis.commands." + cmdName + "Command";
        try {
            Class<?> cls = Class.forName(className);
            Constructor<?> constructor = cls.getConstructor(List.class);

            args.remove(0); // 去掉 command 名称
            return (ICommand)constructor.newInstance(args);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new BedisException(e);
        }
    }
}
