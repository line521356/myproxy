package com.lucius.proxyee;

import com.lucius.proxyee.exception.HttpProxyExceptionHandle;
import com.lucius.proxyee.intercept.HttpProxyIntercept;
import com.lucius.proxyee.intercept.HttpProxyInterceptInitializer;
import com.lucius.proxyee.intercept.HttpProxyInterceptPipeline;
import com.lucius.proxyee.server.HttpProxyServer;
import com.lucius.proxyee.server.HttpProxyServerConfig;
import com.lucius.proxyee.util.HttpUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;

/**
 * @Author: LiWei
 * @Description 匹配到百度首页时重定向到指定url
 * @Date: 2019/3/4 16:23
 */
public class InterceptRedirectHttpProxyServer {
  public static void main(String[] args) throws Exception {
    HttpProxyServerConfig config = new HttpProxyServerConfig();
    config.setHandleSsl(true);
    new HttpProxyServer()
            .serverConfig(config)
            .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
              @Override
              public void init(HttpProxyInterceptPipeline pipeline) {
                pipeline.addLast(new HttpProxyIntercept() {
                  @Override
                  public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
                                            HttpProxyInterceptPipeline pipeline) throws Exception {
                    //匹配到百度首页跳转到淘宝
                    if (HttpUtil.checkUrl(pipeline.getHttpRequest(), "^www.baidu.com$")) {
                      HttpResponse hookResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                      hookResponse.setStatus(HttpResponseStatus.FOUND);
                      hookResponse.headers().set(HttpHeaderNames.LOCATION, "http://www.taobao.com");
                      clientChannel.writeAndFlush(hookResponse);
                      HttpContent lastContent = new DefaultLastHttpContent();
                      clientChannel.writeAndFlush(lastContent);
                      return;
                    }
                    pipeline.beforeRequest(clientChannel, httpRequest);
                  }
                });
              }
            })
            .httpProxyExceptionHandle(new HttpProxyExceptionHandle() {
              @Override
              public void beforeCatch(Channel clientChannel, Throwable cause) throws Exception {
                cause.printStackTrace();
              }

              @Override
              public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause)
                      throws Exception {
                cause.printStackTrace();
              }
            })
            .start(9999);
  }
}
