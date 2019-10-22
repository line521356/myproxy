package com.lucius.proxyee;

import com.lucius.proxyee.server.HttpProxyServer;
import com.lucius.proxyee.server.HttpProxyServerConfig;

public class HandelSslHttpProxyServer {

  public static void main(String[] args) throws Exception {
    HttpProxyServerConfig config =  new HttpProxyServerConfig();
    config.setHandleSsl(true);
    new HttpProxyServer()
        .serverConfig(config)
        .start(9999);
  }
}
