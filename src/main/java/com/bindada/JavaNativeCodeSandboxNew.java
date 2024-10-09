package com.bindada;

import cn.hutool.core.io.resource.ResourceUtil;
import com.bindada.model.ExecuteCodeRequest;
import com.bindada.model.ExecuteCodeResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


/**
 * 模板模式下java原生效果的实现
 */
@Component
public class JavaNativeCodeSandboxNew extends JavaCodeSandboxTemplate{

    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest executeCodeRequest) {
        return super.execute(executeCodeRequest);
    }

    public static void main(String[] args) {
        JavaNativeCodeSandboxNew javaNativeCodeSandbox = new JavaNativeCodeSandboxNew();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2"));
        String code = ResourceUtil.readStr("testcode/Main.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testcode/unsafecode/SleepError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testcode/unsafecode/MemoryError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testcode/unsafecode/ReadFileError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testcode/unsafecode/WriteFileError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testcode/unsafecode/RunFileError.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.execute(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }
}
