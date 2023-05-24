package com.sutoga.backend.config.security;

import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebFluxConfiguration {

    @Bean
    public NettyServerCustomizer nettyServerCustomizer() {
        return httpServer -> httpServer.httpRequestDecoder(
                httpRequestDecoderSpec -> httpRequestDecoderSpec.maxChunkSize(5242880));
    }
}


