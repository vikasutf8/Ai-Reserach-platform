package com.springai.airesearchagent.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.Proxy;

@Configurable
@ConfigurationProperties(prefix = "spring.proxy") // what level ur data present in application.yaml file
@Data
@Slf4j
public class ProxyConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    private String useragent;

    public Proxy toProxy() {
        log.info("Configuring proxy with host: {} and port: {}", host, port);
        return new Proxy(Proxy.Type.HTTP, new java.net.InetSocketAddress(host, port));
    }
}
