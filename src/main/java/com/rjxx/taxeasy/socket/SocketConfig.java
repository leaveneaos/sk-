package com.rjxx.taxeasy.socket;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Created by Administrator on 2016/10/24.
 */
@Component
public class SocketConfig {

    private static Logger logger = LoggerFactory.getLogger(SocketConfig.class);

    @Value("${socket.port:15000}")
    private int port;

    @PostConstruct
    public void initialize() throws IOException {

        // Create an Acceptor
        NioSocketAcceptor acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() + 1);

        // Add Handler
        acceptor.setHandler(new ServerHandler());

//        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        TextLineCodecFactory textLineCodecFactory = new TextLineCodecFactory(Charset.forName("UTF-8"));
        textLineCodecFactory.setDecoderMaxLineLength(Integer.MAX_VALUE);
        textLineCodecFactory.setEncoderMaxLineLength(Integer.MAX_VALUE);
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(textLineCodecFactory));
        acceptor.getSessionConfig().setReadBufferSize(2048*10);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 120);
        // Create Session Configuration
        acceptor.getSessionConfig().setReuseAddress(true);
        logger.info("Starting Server......");
        // Bind and be ready to listen
        acceptor.bind(new InetSocketAddress(port));
        logger.info("Server listening on " + port);
    }

}
