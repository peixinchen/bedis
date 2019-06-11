package com.bittech.bedis.protocol;

import com.bittech.bedis.commands.CommandFactory;
import com.bittech.bedis.commands.ICommand;
import com.bittech.bedis.exceptions.BedisException;
import com.bittech.bedis.io.BedisInputStream;
import com.bittech.bedis.io.BedisOutputStream;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Protocol {
    public static final String CHARSET = "UTF-8";

    public static Charset getCharset() {
        return Charset.forName(CHARSET);
    }

    public static Object read(BedisInputStream is) {
        return process(is);
    }

    public static ICommand readCommand(BedisInputStream is) throws BedisException {
        Object object = read(is);
        if (!(object instanceof List)) {
            throw new BedisException("命令必须是 arrays 格式");
        }

        return CommandFactory.buildFromServer((List<Object>)object);
    }

    public static void writeCommand(BedisOutputStream os, ICommand command) throws BedisException {
        List<Object> list = command.getSerializedArgs();
        list.add(0, command.serialize());
        writeList(os, list);
    }

    public static void writeString(BedisOutputStream os, String s) throws BedisException {
        os.write('+');
        os.write(s.getBytes(getCharset()));
        os.writeCrLf();
    }

    public static void writeError(BedisOutputStream os, String message) throws BedisException {
        os.write('-');
        os.write(message.getBytes(getCharset()));
        os.writeCrLf();
    }

    public static void writeLong(BedisOutputStream os, long value) throws BedisException {
        os.write(':');
        os.writeLong(value);
        os.writeCrLf();
    }

    public static void writeBytes(BedisOutputStream os, byte[] bytes) throws BedisException {
        os.write('$');
        os.writeLong(bytes.length);
        os.writeCrLf();
        os.write(bytes);
        os.writeCrLf();
    }

    public static void writeNull(BedisOutputStream os) throws BedisException {
        os.write('$');
        os.writeLong(-1);
        os.writeCrLf();
    }

    public static void writeList(BedisOutputStream os, List<Object> list) throws BedisException {
        os.write('*');
        os.writeLong(list.size());
        os.writeCrLf();

        for (Object o : list) {
            writeObject(os, o);
        }
    }

    private static void writeObject(BedisOutputStream os, Object o) throws BedisException {
        if (o instanceof String) {
            writeString(os, (String)o);
        } else if (o instanceof Integer) {
            writeLong(os, (Integer)o);
        } else if (o instanceof Long) {
            writeLong(os, (Long)o);
        } else if (o instanceof byte[]) {
            writeBytes(os, (byte[])o);
        } else if (o instanceof List) {
            writeList(os, (List<Object>)o);
        } else if (o instanceof Throwable) {
            Throwable throwable = (Throwable)o;
            writeError(os, throwable.getMessage());
        } else {
            throw new BedisException("写入不认识的数据类型");
        }
    }

    public static void writeNullArray(BedisOutputStream os) throws BedisException {
        os.write('*');
        os.writeLong(-1);
        os.writeCrLf();
    }

    private static Object process(BedisInputStream is) throws BedisException {
        byte b = is.readByte();
        switch (b) {
            case '+':
                return processString(is);
            case '-':
                throw processException(is);
            case ':':
                return processLong(is);
            case '$':
                return processBytes(is);
            case '*':
                return processList(is);
            default:
                throw new BedisException("读到不认识的数据类型");
        }
    }

    private static String processString(BedisInputStream is) throws BedisException {
        return is.readLine();
    }

    private static BedisException processException(BedisInputStream is) {
        try {
            String message = is.readLine();
            throw new BedisException(message);
        } catch (BedisException e) {
            return e;
        }
    }

    private static long processLong(BedisInputStream is) throws BedisException {
        return is.readLongCrLf();
    }

    private static byte[] processBytes(BedisInputStream is) throws BedisException {
        int len = (int)is.readLongCrLf();
        if (len == -1) {
            return null;
        }

        // TODO: 如果对方一直发不送够 len 的数据，这里没有处理
        byte[] buf = new byte[len];
        int nRead = 0;
        while (nRead < len) {
            int size = is.read(buf, nRead, len - nRead);
            if (size == -1) {
                throw new BedisException("中途遇到对方关闭连接了");
            }
            nRead += size;
        }

        is.readByte();  // CR
        is.readByte();  // LF

        return buf;
    }

    private static List<Object> processList(BedisInputStream is) throws BedisException {
        int len = (int)is.readLongCrLf();
        if (len == -1) {
            return null;
        }

        List<Object> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            try {
                Object o = process(is);
                list.add(o);
            } catch (BedisException e) {
                // TODO: 这里没有区分出错误发生到底是对方发送的还是自己这边产生的
                list.add(e);
            }
        }

        return list;
    }
}
