package com.lucius.proxyee.crawler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lucius.proxyee.download.DownloadDouyinVideo;
import com.lucius.proxyee.intercept.HttpProxyIntercept;
import com.lucius.proxyee.intercept.HttpProxyInterceptInitializer;
import com.lucius.proxyee.intercept.HttpProxyInterceptPipeline;
import com.lucius.proxyee.intercept.common.CertDownIntercept;
import com.lucius.proxyee.intercept.common.FullResponseIntercept;
import com.lucius.proxyee.queue.MyQueue;
import com.lucius.proxyee.server.HttpProxyServer;
import com.lucius.proxyee.server.HttpProxyServerConfig;
import com.lucius.proxyee.util.FileUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.internal.StringUtil;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Tiktok {

    public static void main(String[] args) {
        //启动代理服务线程
        new Thread(Tiktok::proxyFun).start();
        //这个数字是下载线程数量，数字越大下载越快，最小得1
        int count = 4;
        DownloadDouyinVideo downloadDouyinVideo = new DownloadDouyinVideo("D:\\tiktok\\video\\","tiktok_url");
        for (int i = 0; i < count; i++) {
            new Thread(downloadDouyinVideo::downloadFun).start();
        }

        System.out.println("\033[1;33m***************************************************************************************************************************************************************************\033[33m");
        System.out.println("\033[1;33m***************************************************************************************************************************************************************************\033[33m");
        System.out.println("\033[1;33m***************************************************************************启    动    成    功****************************************************************************\033[33m");
        System.out.println("\033[1;33m***************************************************************************************************************************************************************************\033[33m");
        System.out.println("\033[1;33m***************************************************************************************************************************************************************************\033[33m");
        System.out.println("\033[1;31m请用手机连接代理开始刷tiktokv3.5\033[31m");
    }
    public static void proxyFun(){
        HttpProxyServerConfig config = new HttpProxyServerConfig();
        config.setHandleSsl(true);
        new HttpProxyServer()
                .serverConfig(config)
                .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
                    @Override
                    public void init(HttpProxyInterceptPipeline pipeline) {
                        pipeline.addLast(new CertDownIntercept());
                        pipeline.addLast(new HttpProxyIntercept() {
                            @Override
                            public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
                                                      HttpProxyInterceptPipeline pipeline) throws Exception {
                                String uri = httpRequest.uri();
                                String interceptUri = "aweme";
                                String [] us = new String[]{"feed"};
                                if(uri.contains(interceptUri)){
                                    uri = uri.replace("mcc_mnc=46000","mcc_mnc=45418");
                                    uri = uri.replace("carrier_region=CN","carrier_region=HK");
                                    httpRequest.setUri(uri);
                                    System.out.println(uri);
                                }
                                //转到下一个拦截器处理
                                pipeline.beforeRequest(clientChannel, httpRequest);
                            }


                        });
                        pipeline.addLast(new FullResponseIntercept() {

                            FileUtil fileUtil = new FileUtil("D:\\tiktok\\csv\\");

                            @Override
                            public boolean match(HttpRequest httpRequest, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
                                List<String> models = new ArrayList<>();
                                //推送
                                models.add("/aweme/v1/feed");
                                String uri = pipeline.getHttpRequest().uri();
                                for (String model : models) {
                                    if(uri.contains(model)){
                                        return true;
                                    }
                                }
                                return false;
                            }

                            private void video(FullHttpResponse httpResponse){
                                String content = httpResponse.content().toString(Charset.defaultCharset());
                                JSONObject json = JSONObject.parseObject(content);
                                System.out.println(json);
                                JSONArray awemeList = json.getJSONArray("aweme_list");
                                for (Object o : awemeList) {
                                    JSONObject aweme = JSONObject.parseObject(o.toString());
                                    try {
                                        String id = aweme.getString("aweme_id");
                                        String desc = aweme.getString("desc");
                                        String url = null;
                                        String starCount = aweme.getJSONObject("statistics").getString("digg_count");
                                        String commentCount = aweme.getJSONObject("statistics").getString("comment_count");
                                        String shareCount = aweme.getJSONObject("statistics").getString("share_count");
                                        String downloadCount = aweme.getJSONObject("statistics").getString("download_count");
                                        url = aweme.getJSONObject("video").getJSONObject("play_addr").getJSONArray("url_list").get(0).toString();
                                        MyQueue.push("tiktok_url",id+"###"+url);
                                        String result = id + "," + url + "," + desc + "," + starCount + "," +commentCount + "," + shareCount + "," +downloadCount;
                                        fileUtil.writeLine(result,"video");
                                    }catch (Exception e){
                                        continue;
                                    }
                                }
                            }

                            @Override
                            public void handelResponse(HttpRequest httpRequest, FullHttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
                                String content = httpResponse.content().toString(Charset.defaultCharset());
                                JSONObject json = JSONObject.parseObject(content);
                                System.out.println(json);
                                video(httpResponse);


                            }
                        });
                    }
                })
                .start(9999);
    }
}
