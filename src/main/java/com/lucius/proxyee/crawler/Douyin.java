package com.lucius.proxyee.crawler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lucius.proxyee.download.DownloadDouyinVideo;
import com.lucius.proxyee.intercept.HttpProxyInterceptInitializer;
import com.lucius.proxyee.intercept.HttpProxyInterceptPipeline;
import com.lucius.proxyee.intercept.common.CertDownIntercept;
import com.lucius.proxyee.intercept.common.FullResponseIntercept;
import com.lucius.proxyee.queue.MyQueue;
import com.lucius.proxyee.server.HttpProxyServer;
import com.lucius.proxyee.server.HttpProxyServerConfig;
import com.lucius.proxyee.util.FileUtil;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.internal.StringUtil;

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
                                //评论
                                models.add("/aweme/v2/comment/list/");
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
                                    try {
                                        JSONObject comment = JSONObject.parseObject(o.toString());
                                        String id = comment.getString("aweme_id");
                                        String userId = comment.getJSONObject("user").getString("uid");
                                        String userName = comment.getJSONObject("user").getString("nickname");
                                        String commentStr = comment.getString("text");
                                        String starCount = comment.getString("digg_count");
                                        String result = id + "," + userId + "," + userName + "," + commentStr + "," + starCount;
                                        FileUtil.writeLine(result,"comment");
                                    } catch (Exception e) {
                                        //一个字段抓不到 这条数据我就不要了
                                        e.printStackTrace();
                                        continue;
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
                                    try {
                                        String id = aweme.getString("aweme_id");
                                        String desc = aweme.getString("desc");
                                        String url = null;
                                        String starCount = aweme.getJSONObject("statistics").getString("digg_count");
                                        String commentCount = aweme.getJSONObject("statistics").getString("comment_count");
                                        String shareCount = aweme.getJSONObject("statistics").getString("share_count");
                                        String downloadCount = aweme.getJSONObject("statistics").getString("download_count");
                                        url = aweme.getJSONObject("video").getJSONObject("play_addr").getJSONArray("url_list").get(0).toString();
                                        MyQueue.push("douyin_url",id+"###"+url);
                                        String result = id + "," + url + "," + desc + "," + starCount + "," +commentCount + "," + shareCount + "," +downloadCount;
                                        FileUtil.writeLine(result,"video");
                                    }catch (Exception e){
                                        //一个字段抓不到 这条数据我就不要了
                                        continue;
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
