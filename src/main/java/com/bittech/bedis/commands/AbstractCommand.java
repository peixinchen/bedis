package com.bittech.bedis.commands;

import com.bittech.bedis.exceptions.BedisException;
import com.bittech.bedis.io.BedisInputStream;
import com.bittech.bedis.io.BedisOutputStream;
import com.bittech.bedis.protocol.Protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

abstract public class AbstractCommand implements ICommand {
    protected static final Logger logger = LoggerFactory.getLogger("bedis");

    protected List<Object> args;

    public AbstractCommand() {
    }

    public AbstractCommand(List<Object> args) {
        this.args = args;
    }

    protected abstract void processInner(BedisOutputStream os) throws BedisException;
    protected abstract void displayReplyInner(Object object) throws BedisException;

    @Override
    public void process(BedisOutputStream os) throws BedisException {
        processInner(os);
    }

    @Override
    public void displayReply(BedisInputStream is) throws BedisException {
        Object o = Protocol.read(is);
        if (o instanceof BedisException) {
            BedisException e = (BedisException)o;
            System.out.println("error) " + e.getMessage());
            return;
        }

        displayReplyInner(o);
    }
}
