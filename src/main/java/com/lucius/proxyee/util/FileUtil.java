package com.lucius.proxyee.util;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class FileUtil {

    private final static String filePath = "D:\\douyin\\csv\\";

    private final static String fileName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_douyin.csv";


    public static void writeLine(String text,String type) throws IOException {
        File file = new File(filePath + type + fileName);
        boolean flag = file.exists();
        RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
        if(!flag){
            file.createNewFile();
            if(type.equals("video")){
                randomFile.write("视频id,视频链接,文案内容,点赞数量,评论数量,分享数量,下载数量\r\n".getBytes());
            }else if(type.equals("comment")){
                randomFile.write("视频id,用户id,用户名,评论内容,点赞数\r\n".getBytes());
            }


        }
        long fileLength = randomFile.length();
        randomFile.seek(fileLength);
        randomFile.write((text+"\r\n").getBytes());
        randomFile.close();
    }
}
