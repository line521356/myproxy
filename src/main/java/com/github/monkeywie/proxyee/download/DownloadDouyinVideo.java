package com.github.monkeywie.proxyee.download;

import com.github.monkeywie.proxyee.util.DownloadUtil;
import com.github.monkeywie.proxyee.util.RedisUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadDouyinVideo {

    public static boolean httpDownload(String httpUrl, String saveFile) {
        // 1.下载网络文件
        int byteRead;
        URL url;
        try {
            url = new URL(httpUrl);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            return false;
        }

        try {
            //2.获取链接
            URLConnection conn = url.openConnection();
            //3.输入流
            InputStream inStream = conn.getInputStream();
            //3.写入文件
            FileOutputStream fs = new FileOutputStream(saveFile);

            byte[] buffer = new byte[1024];
            while ((byteRead = inStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteRead);
            }
            inStream.close();
            fs.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void downloadFun(){
        while (true) {
            String result = RedisUtil.getRedisUtil().rpop("douyin_url");
            if (result == null) {
                try {
                    Thread.sleep(10*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            String[] results = result.split("###");
            String videoUrl = results[1];
            String id = results[0];
            String saveFile = "D:\\douyin\\video\\" + id + ".mp4";
            try {
                System.out.println("正在下载：" + videoUrl);
                httpDownload(videoUrl, saveFile);
            } catch (Exception e) {
                continue;
            }
        }
    }

    public static void main(String[] args) {
        downloadFun();
    }
}
