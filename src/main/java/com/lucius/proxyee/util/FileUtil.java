package com.lucius.proxyee.util;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class FileUtil {

    private final static String filePath = "D:\\douyin\\csv\\";

    public  static void writeLine(String text,String type) throws IOException {
        String fileName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_douyin.csv";
        File file = new File(filePath + type + fileName);
        synchronized (FileUtil.class) {
            if (!file.exists()) {
                String firstLine = null;
                if (type.equals("video")) {
                    firstLine = "视频id,视频链接,文案内容,点赞数量,评论数量,分享数量,下载数量\r\n";
                } else if (type.equals("comment")) {
                    firstLine = "\"视频id,用户id,用户名,评论内容,点赞数\r\n\"";
                }
                assert firstLine != null;
                FileUtils.openOutputStream(file, true).write(firstLine.getBytes());
            }
            FileUtils.openOutputStream(file, true).write(text.getBytes());
        }
    }
}
