package com.bindada.utils;

import com.bindada.model.ExecuteMessage;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 进程工具类
 */
public class ProcessUtil {
    /**
     * 执行程序并封装运行信息
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) throws InterruptedException, IOException {
        ExecuteMessage executeMessage = new ExecuteMessage();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int exitValue = runProcess.waitFor();  //等待程序结束获得执行码  0表示正常结束，其他则异常
        executeMessage.setExitValue(exitValue);
        if(exitValue==0){
            //正常退出
            System.out.println(opName+"成功");
            //分批获取进程的正常输出
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
            StringBuilder compileOutputStringBuilder = new StringBuilder();
            String compileOutputLine;
            while((compileOutputLine=bufferedReader.readLine())!=null){
                compileOutputStringBuilder.append(compileOutputLine);
            }
            executeMessage.setMessage(compileOutputStringBuilder.toString());
        }else {
            //异常退出
            System.out.println(opName+"失败，错误码： "+exitValue);
            //分批获取进程的正常输出
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
            StringBuilder compileOutputStringBuilder = new StringBuilder();
            String compileOutputLine;
            while((compileOutputLine=bufferedReader.readLine())!=null){
                compileOutputStringBuilder.append(compileOutputLine);
            }
            //分批获取进程的错误输出
            BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
            StringBuilder errorCompileOutputStringBuilder = new StringBuilder();
            String errorCompileOutputLine;
            while((errorCompileOutputLine=errorBufferedReader.readLine())!=null){
                errorCompileOutputStringBuilder.append(errorCompileOutputLine);
            }
            executeMessage.setErrorMessage(compileOutputStringBuilder.toString());
            executeMessage.setErrorMessage(errorCompileOutputStringBuilder.toString());
        }
        stopWatch.stop();
        executeMessage.setTime(stopWatch.getLastTaskTimeMillis());

        return executeMessage;
    }
}
