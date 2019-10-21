package com.github.monkeywie.proxyee.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class FileUtil {

//    private final static String filePath = "D:\\douyin\\csv\\";
    private final static String filePath = "D:\\tiktok\\csv\\";

    private final static String fileName = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) + "_douyin.csv";

    private static FileWriter fileWriter = null;

    static {
        try {
            fileWriter = new FileWriter(new File(filePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void writeLine(String text) throws IOException {
        fileWriter.write(text + "\r\n");
        fileWriter.flush();
    }
}
