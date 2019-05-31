package com.bittech.bedis.io;

import com.bittech.bedis.exceptions.BedisException;
import com.bittech.bedis.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class BedisInputStream extends FilterInputStream {
    private static final Logger logger = LoggerFactory.getLogger("bedis");
    private final int BUF_SIZE = 8192;
    private final byte[] buf = new byte[BUF_SIZE];
    private int count = 0;
    private int limit = 0;

    public BedisInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read(byte[] b, int off, int len) throws BedisException {
        ensureFill();
        len = Math.min(remain(), len);
        System.arraycopy(buf, count, b, off, len);
        count += len;

        return len;
    }

    public byte readByte() throws BedisException {
        ensureFill();
        return buf[count++];
    }

    public byte[] readLineBytes() throws BedisException {
        List<Byte> bytes = new LinkedList<>();
        while (true) {
            ensureFill();

            byte b = buf[count++];
            if (b == '\r') {
                ensureFill();
                byte c = buf[count++];
                if (c == '\n') {
                    break;
                }

                bytes.add(b);
                bytes.add(c);
            } else {
                bytes.add(b);
            }
        }

        if (bytes.size() == 0) {
            throw new BedisException("一个字节都没有读到");
        }

        return toPrimitive(bytes);
    }

    public String readLine() throws BedisException {
        return new String(readLineBytes(), Protocol.getCharset());
    }

    public long readLongCrLf() throws BedisException {
        ensureFill();
        boolean isNegative = (buf[count] == '-');
        if (isNegative) {
            count++;
        }

        long value = 0;
        while (true) {
            ensureFill();

            byte b = buf[count++];
            if (b == '\r') {
                ensureFill();

                if (buf[count++] != '\n') {
                    throw new BedisException("没有 CRLF 结尾");
                }

                break;
            } else {
                value = value * 10 + (b - '0');
            }
        }

        return isNegative ? -value : value;
    }

    private int remain() {
        return limit - count;
    }

    private void ensureFill() throws BedisException {
        if (count >= limit) {
            try {
                limit = in.read(buf, 0, BUF_SIZE);
                count = 0;
                if (limit == -1) {
                    throw new BedisException("不应该出现的 End of stream");
                }
            } catch (IOException e) {
                throw new BedisException(e);
            }
        }
    }

    private static byte[] toPrimitive(List<Byte> bytes) {
        if (bytes == null) {
            return new byte[0];
        }

        byte[] array = new byte[bytes.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = bytes.get(i);
        }

        return array;
    }
}
