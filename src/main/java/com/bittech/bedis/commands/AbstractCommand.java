package com.bittech.bedis.commands;

import com.bittech.bedis.exceptions.BedisException;
import com.bittech.bedis.io.BedisOutputStream;

import java.util.List;

abstract public class AbstractCommand implements ICommand {
    protected List<Object> args;

    public AbstractCommand() {
    }

    public AbstractCommand(List<Object> args) {
        this.args = args;
    }

    protected abstract void processInner(BedisOutputStream os) throws BedisException;

    @Override
    public void process(BedisOutputStream os) throws BedisException {
        processInner(os);
    }
}
