package com.lucius.proxyee.download;

import com.lucius.proxyee.queue.MyQueue;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadDouyinVideo {

    private String videoLocalUrl;

    private String key;


    public DownloadDouyinVideo(String videoLocalUrl, String key) {
        this.videoLocalUrl = videoLocalUrl;
        this.key = key;
    }

    public String getVideoLocalUrl() {
        return videoLocalUrl;
    }

    public void setVideoLocalUrl(String videoLocalUrl) {
        this.videoLocalUrl = videoLocalUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private boolean httpDownload(String httpUrl, String saveFile) {
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

    public void downloadFun(){
        File file = new File(this.videoLocalUrl);
        if(!file.exists()){
            file.mkdir();
        }
        while (true) {
            String result = MyQueue.pop(key);
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
            String saveFile = videoLocalUrl + id + ".mp4";
            try {
                System.out.println("正在下载：" + videoUrl);
                System.out.println("当前还剩下：" + MyQueue.length(key));
                httpDownload(videoUrl, saveFile);
            } catch (Exception e) {
                continue;
            }
        }
    }

}
