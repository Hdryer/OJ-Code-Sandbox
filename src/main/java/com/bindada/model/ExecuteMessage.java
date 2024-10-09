package com.bindada.model;

import lombok.Data;

/**
 * 程序执行返回封装
 */
@Data
public class ExecuteMessage {

    /**
     * 程序执行退出码
     */
    private Integer exitValue;

    /**
     * 正常输出信息
     */
    private String message;

    /**
     * 异常输出信息
     */
    private String errorMessage;

    /**
     * 执行程序所耗时间
     */
    private Long time;

    /**
     * 执行程序所占内存
     */
    private Long memory;
}
