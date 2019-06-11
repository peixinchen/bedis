package com.bittech.bedis.commands;

import com.bittech.bedis.exceptions.BedisException;
import com.bittech.bedis.io.BedisInputStream;
import com.bittech.bedis.io.BedisOutputStream;
import com.bittech.bedis.protocol.Protocol;

import java.util.List;
import java.util.Scanner;
import java.util.Arrays;

public interface ICommand {
    default String getName() {
        String[] fragments = getClass().getName().split("\\.");
        return fragments[fragments.length - 1].replace("Command", "");
    }

    default byte[] serialize() {
        return getName().getBytes(Protocol.getCharset());
    }

    void readArgs(Scanner scanner);

    List<Object> getSerializedArgs();

    void process(BedisOutputStream os) throws BedisException;

    void displayReply(BedisInputStream is) throws BedisException;
}
