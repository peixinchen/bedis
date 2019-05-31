package com.bittech.bedis.io;

import com.bittech.bedis.exceptions.BedisException;
import com.bittech.bedis.protocol.Protocol;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BedisOutputStream extends FilterOutputStream {
    private int BUF_SIZE = 8192;
    private byte[] buf = new byte[BUF_SIZE];
    private int count = 0;

    public BedisOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) throws BedisException {
        if (remain() < 1) {
            flushBuffer();
        }

        buf[count++] = (byte)b;
    }

    @Override
    public void write(byte[] b) throws BedisException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws BedisException {
        if (len >= buf.length) {
            flushBuffer();
            try {
                out.write(b, off, len);
            } catch (IOException e) {
                throw new BedisException(e);
            }
        } else {
            if (len >= remain()) {
                flushBuffer();
            }

            System.arraycopy(b, off, buf, count, len);
            count += len;
        }
    }

    public void writeCrLf() throws BedisException {
        if (remain() < 2) {
            flushBuffer();
        }

        buf[count++] = '\r';
        buf[count++] = '\n';
    }

    public void writeLong(long value) throws BedisException {
        if (value < 0) {
            write('-');
            value = -value;
        }

        byte[] valueBytes = Long.toString(value).getBytes(Protocol.getCharset());
        write(valueBytes);
    }

    @Override
    public void flush() throws BedisException {
        flushBuffer();
        try {
            out.flush();
        } catch (IOException e) {
            throw new BedisException(e);
        }
    }

    private int remain() {
        return buf.length - count;
    }

    private void flushBuffer() throws BedisException {
        if (count <= 0) {
            return;
        }

        try {
            out.write(buf, 0, count);
        } catch (IOException e) {
            throw new BedisException(e);
        }
        count = 0;
    }
}
