package com.example.service;

import com.example.inteceptor.TestServerInterceptor;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pb.ApiServerGrpc;
import pb.XxxApi;

/**
 * @author ：lw
 * @date ：Created in 2021/8/31 11:14
 */
@GrpcService(interceptors = TestServerInterceptor.class)
public class TestGrpcServer extends ApiServerGrpc.ApiServerImplBase {

    private final Logger logger = LoggerFactory.getLogger(TestGrpcServer.class);

    @Override
    public void getUserInfo(XxxApi.GetUserInfoReq request, StreamObserver<XxxApi.GetUserInfoRes> responseObserver) {
        try {
            logger.info("请求参数：userName = " + request.getUserName());
            XxxApi.GetUserInfoRes res = XxxApi.GetUserInfoRes.newBuilder().setAddress("重庆 渝中 解放碑XXX号").setUserName("username")
                    .build();
            responseObserver.onNext(res);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
