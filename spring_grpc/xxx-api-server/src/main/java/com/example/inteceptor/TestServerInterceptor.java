package com.example.inteceptor;

import com.example.service.TestGrpcServer;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author ：lw
 * @date ：Created in 2021/8/31 18:11
 */
public class TestServerInterceptor implements ServerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(TestServerInterceptor.class);

    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        Metadata.Key<String> token = Metadata.Key.of("token", Metadata.ASCII_STRING_MARSHALLER);
        String tokenStr = headers.get(token);
        if (StringUtils.isEmpty(tokenStr)) {
            logger.info("token is empty");
            call.close(Status.DATA_LOSS, headers);
        }
        logger.info("token: " + tokenStr);
        return next.startCall(call, headers);
    }
}
