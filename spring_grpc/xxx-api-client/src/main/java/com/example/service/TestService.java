package com.example.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import pb.ApiServerGrpc;
import pb.XxxApi;

/**
 * @author ：lw
 * @date ：Created in 2021/8/31 11:56
 */
@Service
public class TestService {
    @GrpcClient(value = "xxx-api-server", interceptorNames = {"testInterceptor"})
    private ApiServerGrpc.ApiServerBlockingStub serverBlockingStub;

    public void test() {
        XxxApi.GetUserInfoRes res = serverBlockingStub.getUserInfo(XxxApi.GetUserInfoReq.newBuilder().setUserName("ljw").build());
        System.out.println(res);
    }
}
