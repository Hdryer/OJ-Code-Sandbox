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
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 判题程序实现（java原生实现代码沙箱）
 */
@Component
public class JavaNativeCodeSandbox implements CodeSandbox{

    private static  final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static  final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    public static final long TIME_OUT = 10*1000L;

    public static final List<String> blackList  = Arrays.asList("Files","exec","File");

    public static final WordTree WORD_TREE;

    static {
        // 初始化字典树
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(blackList);
    }

    public static void main(String[] args) {
        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2"));
//        String code = ResourceUtil.readStr("testcode/Main.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testcode/unsafecode/SleepError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testcode/unsafecode/MemoryError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testcode/unsafecode/ReadFileError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testcode/unsafecode/WriteFileError.java", StandardCharsets.UTF_8);
        String code = ResourceUtil.readStr("testcode/unsafecode/RunFileError.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.execute(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        String userDir = System.getProperty("user.dir");
        String globalCodePathName  = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在，没有则新建文件目录
        if(!FileUtil.exist(globalCodePathName)){
            FileUtil.mkdir(globalCodePathName);
        }

        // 校验代码，防止有敏感程序
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if(foundWord!=null){
            System.out.println("包含禁止词: "+foundWord.getFoundWord());
            return null;
        }
        /** 1、把用户提交的代码存放起来，通过UUID生成区分文件名 */
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        /** 2、编译代码，得到Class文件*/
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtil.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);
        } catch (Exception e) {
            return getErrorResponse(e);
        }

        /** 3、执行代码，得到输出结果*/
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
                return getErrorResponse(e);
            }
        }

        /** 4、整理用户代码输出结果*/
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

        /** 5、文件清理,防止服务器空间不足 */
        if(userCodeFile.getParentFile()!=null){
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除"+(del? "成功":"失败"));
        }

        return executeCodeResponse;
    }


    /**
     * 统一获取错误响应
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
