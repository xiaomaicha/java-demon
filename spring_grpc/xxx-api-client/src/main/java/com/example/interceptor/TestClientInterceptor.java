package com.example.interceptor;

import io.grpc.*;

/**
 * @author ：lw
 * @date ：Created in 2021/8/31 19:18
 */
public class TestClientInterceptor implements ClientInterceptor {
    Metadata.Key<String> token = Metadata.Key.of("token", Metadata.ASCII_STRING_MARSHALLER);

    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener responseListener, Metadata headers) {
                headers.put(token, "AAAAAAAAAAAAAAAAAAA");
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                        /**
                         * if you don't need receive header from server, you can
                         * use {@link io.grpc.stub.MetadataUtils#attachHeaders}
                         * directly to send header
                         */
                        System.out.println("header received from server:" + headers);
                        super.onHeaders(headers);
                    }
                }, headers);
            }
        };
    }
}
