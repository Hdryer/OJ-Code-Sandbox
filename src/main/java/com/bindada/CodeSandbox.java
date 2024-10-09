package com.bindada;


import com.bindada.model.ExecuteCodeRequest;
import com.bindada.model.ExecuteCodeResponse;

/**
 * 代码沙箱的接口定义
 */
public interface CodeSandbox {

    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse execute(ExecuteCodeRequest executeCodeRequest);

}
