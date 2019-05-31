package com.bittech.bedis.commands;

import com.bittech.bedis.exceptions.BedisException;
import com.bittech.bedis.io.BedisOutputStream;
import com.bittech.bedis.protocol.Protocol;

import java.util.List;
import java.util.Scanner;

public interface ICommand {
    default String getName() {
        return getClass()
                .getName()
                .replace("Command", "");
    }

    default byte[] serialize() {
        return getName().getBytes(Protocol.getCharset());
    }

    void readArgs(Scanner scanner);

    List<Object> getSerializedArgs();

    void process(BedisOutputStream os) throws BedisException;
}
