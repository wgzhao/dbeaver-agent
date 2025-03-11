package dev.misakacloud.dbee.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class LogUtils {
    private static final String logFile = "d:/dbeaver-agent-logs.txt";
    private static final Charset utf8 = StandardCharsets.UTF_8;

    private static final boolean writeToFile = true;
    private static final boolean writeToUDP = false;

    public static void initialize() {
        try {
            if (writeToFile) {
                Files.deleteIfExists(Paths.get(logFile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToFile(String msg, String level) {
        try {
            String output = buildMsg(msg, level);
            //Files.write(Paths.get(logFile), Arrays.asList(output), utf8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            FileOutputStream fos = new FileOutputStream(new File(logFile), true);
            fos.write(output.getBytes(utf8));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String buildMsg(String msg, String level) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String output = "[" + formatter.format(date) + "]" + "[" + level + "]" + msg + "\r\n";
        return output;
    }

    private static void writeToUDP(String msg, String level) {
        try {

            String output = buildMsg(msg, level);
            byte[] message = output.getBytes(utf8);
            InetAddress address = InetAddress.getByName("192.168.1.2");
            DatagramPacket packet = new DatagramPacket(message, message.length, address, 8068);
            DatagramSocket dsocket = new DatagramSocket();
            dsocket.send(packet);
            dsocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void write(String msg, String level) {
        if (writeToFile) {
            writeToFile(msg, level);
        }
        if (writeToUDP) {
            writeToUDP(msg, level);
        }
    }

    public static void debug(String msg) {
        write(msg, "DBG");
    }

    public static void error(String msg) {
        write(msg, "ERR");
    }
}