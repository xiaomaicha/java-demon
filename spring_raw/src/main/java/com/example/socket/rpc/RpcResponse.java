package com.example.socket.rpc;


/**
 * @author by dell
 * @Classname RpcResponse
 * @Description 返回实体类源码
 * @Date 2022/8/21 14:56
 */

public class RpcResponse {
    /**
     * 请求流水号
     */
    private String requestId;
    /**
     * 异常
     */
    private Exception exception;
    /**
     * 返回结果
     **/
    private Object result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}