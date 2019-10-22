package com.github.monkeywie.proxyee.crawler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.monkeywie.proxyee.download.DownloadDouyinVideo;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.CertDownIntercept;
import com.github.monkeywie.proxyee.intercept.common.FullRequestIntercept;
import com.github.monkeywie.proxyee.intercept.common.FullResponseIntercept;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.util.FileUtil;
import com.github.monkeywie.proxyee.util.HttpUtil;
import com.github.monkeywie.proxyee.util.RedisUtil;
import com.sun.xml.internal.ws.util.StringUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.internal.StringUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Douyin {

    public static void main(String[] args) {
        //启动代理服务线程
        new Thread(Douyin::proxyFun).start();
        //这个数字是下载线程数量，数字越大下载越快，最小得1
        int count = 1;
        for (int i = 0; i < count; i++) {
            new Thread(DownloadDouyinVideo::downloadFun).start();
        }
        for (int i = 0; i < 100; i++) {
            System.out.println("启动成功");
        }
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
                        pipeline.addLast(new FullResponseIntercept() {

                            @Override
                            public boolean match(HttpRequest httpRequest, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
                                List<String> models = new ArrayList<>();
                                //喜欢
                                models.add("/aweme/v1/aweme/favorite/");
                                String uri = pipeline.getHttpRequest().uri();
                                for (String model : models) {
                                    if(uri.contains(model)){
                                        return true;
                                    }
                                }
                                return false;
                            }

                            private void comment(FullHttpResponse httpResponse){
                                String content = httpResponse.content().toString(Charset.defaultCharset());
                                JSONObject json = JSONObject.parseObject(content);
                                System.out.println(json);
                                JSONArray commentList = json.getJSONArray("comments");
                                for (Object o : commentList) {
                                    JSONObject comment = JSONObject.parseObject(o.toString());
                                    String id = comment.getString("aweme_id");
                                    String userId = comment.getJSONObject("user").getString("uid");
                                    String userName = comment.getJSONObject("user").getString("nickname");
                                    String commentStr = comment.getString("text");
                                    String starCount = comment.getString("digg_count");
                                    String result = id + "," + userId + "," + userName + "," + commentStr + "," + starCount;
                                    try {
                                        FileUtil.writeLine(result,"comment");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            private void video(FullHttpResponse httpResponse){
                                String content = httpResponse.content().toString(Charset.defaultCharset());
                                JSONObject json = JSONObject.parseObject(content);
                                System.out.println(json);
                                JSONArray awemeList = json.getJSONArray("aweme_list");
                                for (Object o : awemeList) {
                                    JSONObject aweme = JSONObject.parseObject(o.toString());
                                    String id = aweme.getString("aweme_id");
                                    String desc = aweme.getString("desc");
                                    String url = null;
                                    String starCount = null;
                                    try {
                                        url = aweme.getJSONObject("video").getJSONObject("play_addr").getJSONArray("url_list").get(0).toString();
                                    }catch (Exception e){
                                        continue;
                                    }
                                    RedisUtil.getRedisUtil().lpush("douyin_url",id+"###"+url);
                                    String result = id + "," + url + "," + desc + "," + starCount;
                                    try {
                                        FileUtil.writeLine(result,"video");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void handelResponse(HttpRequest httpRequest, FullHttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
                                String content = httpResponse.content().toString(Charset.defaultCharset());
                                JSONObject json = JSONObject.parseObject(content);
                                System.out.println(json);
                                if(StringUtil.isNullOrEmpty(json.getString("aweme_list"))){
                                    comment(httpResponse);
                                }else{
                                    video(httpResponse);
                                }

                            }
                        });
                    }
                })
                .start(9999);
    }

}
