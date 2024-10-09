package com.bindada.controller;

import com.bindada.JavaNativeCodeSandboxNew;
import com.bindada.model.ExecuteCodeRequest;
import com.bindada.model.ExecuteCodeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("/")
public class MainController {

    //定义容器api的健全请求头和密钥
    public static final String AUTH_REQUEST_HEADER = "myAuth";

    public static final String AUTH_REQUEST_SECRET = "secretKey";

    @Resource
    private  JavaNativeCodeSandboxNew javaNativeCodeSandboxNew;

    @GetMapping("/health")
    public String healthCheck(){
        return "hello";
    }

    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/execute")
    public ExecuteCodeResponse execute(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request, HttpServletResponse response){
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if(!authHeader.equals(AUTH_REQUEST_SECRET)){
            response.setStatus(403);
            return null;
        }
        if(executeCodeRequest==null)
            throw new RuntimeException("传入参数为空");
        return javaNativeCodeSandboxNew.execute(executeCodeRequest);
    }
}