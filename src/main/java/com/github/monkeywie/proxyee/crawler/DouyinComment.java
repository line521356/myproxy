package com.github.monkeywie.proxyee.crawler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.CertDownIntercept;
import com.github.monkeywie.proxyee.intercept.common.FullResponseIntercept;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.util.FileUtil;
import com.github.monkeywie.proxyee.util.RedisUtil;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.Charset;

public class DouyinComment {
    public static void main(String[] args) {
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
                                String model1 = "/aweme/v2/comment/list/";
                                String uri = pipeline.getHttpRequest().uri();
                                if(uri.contains(model1)){
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public void handelResponse(HttpRequest httpRequest, FullHttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
                                String content = httpResponse.content().toString(Charset.defaultCharset());
                                System.err.println(content);


                            }
                        });
                    }
                })
                .start(9999);
    }
}
