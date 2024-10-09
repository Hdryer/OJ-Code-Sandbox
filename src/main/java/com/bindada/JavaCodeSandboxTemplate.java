package com.bindada;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.bindada.model.ExecuteCodeRequest;
import com.bindada.model.ExecuteCodeResponse;
import com.bindada.model.ExecuteMessage;
import com.bindada.model.JudgeInfo;
import com.bindada.utils.ProcessUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class JavaCodeSandboxTemplate implements CodeSandbox{


    private static  final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static  final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    public static final long TIME_OUT = 10*1000L;


    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        ExecuteCodeResponse executeCodeResponse = null;

        try {
            /** 1、把用户提交的代码存放起来，通过UUID生成区分文件名 */
            File userCodeFile = saveCodeToFle(code);

            /** 2、编译代码，得到Class文件*/
            ExecuteMessage executeMessag1 = compileFile(userCodeFile);
            System.out.println(executeMessag1);

            /** 3、执行代码，得到输出结果*/
            List<ExecuteMessage> executeMessageList = runFile(userCodeFile,inputList);

            /** 4、整理用户代码输出结果*/
            executeCodeResponse = getOutputResponse(executeMessageList);

            /** 5、文件清理,防止服务器空间不足 */
            delFile(userCodeFile);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
        return executeCodeResponse;
    }


    /**
     * 1、把用户的代码作为参数传保存为文件
     * @param code  用户代码
     * @return
     */
    public File saveCodeToFle(String code){
        String userDir = System.getProperty("user.dir");
        String globalCodePathName  = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在，没有则新建文件目录
        if(!FileUtil.exist(globalCodePathName)){
            FileUtil.mkdir(globalCodePathName);
        }

        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }

    /**
     * 2、编译保存的用户代码为class文件
     * @param userCodeFile  用户代码文件
     * @return
     */
    public ExecuteMessage compileFile(File userCodeFile){
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtil.runProcessAndGetMessage(compileProcess, "编译");
            return executeMessage;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 3、执行文件，获得结果列表
     * @param inputList
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList){
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                // 开启守护线程 超时终止代码
                new Thread(()->{
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时了，终止程序");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                },"守护线程").start();
                ExecuteMessage executeMessage = ProcessUtil.runProcessAndGetMessage(runProcess, "运行");
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                throw  new RuntimeException(e);
            }
        }
        return executeMessageList;
    }


    /**
     * 4、整理用户代码输出结果
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        long maxTime = 0;
        for (ExecuteMessage executeMessage : executeMessageList) {

            String errorMessage = executeMessage.getErrorMessage();
            if(StrUtil.isNotBlank(errorMessage)){
                executeCodeResponse.setMessage(errorMessage);
                //执行中存在错误
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if(time !=null){
                maxTime = Math.max(maxTime, time);
            }
        }

        if(outputList.size() == executeMessageList.size()){
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);

        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        //judgeInfo.setMemory();
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 5、文件清理,防止服务器空间不足
     * @param userCodeFile
     */
    public boolean delFile(File userCodeFile){
        if(userCodeFile.getParentFile()!=null){
            boolean del = FileUtil.del(userCodeFile.getParentFile().getAbsolutePath());
            System.out.println("删除"+(del? "成功":"失败"));
            return del;
        }
        return true;
    }


    /**
     * 6、统一获取错误响应
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());

        return executeCodeResponse;
    }


}
