package com.bindada.security;

import java.security.Permission;

/**
 * 自定义安全管理器，限制代码运行所需要的权限
 */
public class MySecurityManager extends SecurityManager{

    //检查所有权限
    @Override
    public void checkPermission(Permission perm) {
        super.checkPermission(perm);
    }

    //检查程序是否可执行文件
    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("checkExec权限异常"+cmd);
    }

    //检查程序是否允许读文件
    @Override
    public void checkRead(String file) {
        throw new SecurityException("checkRead权限异常"+file);
    }

    // 检查程序是否允许写文件
    @Override
    public void checkWrite(String file) {
        throw new SecurityException("checkWrite权限异常"+file);
    }

    // 检查程序是否删除文件
    @Override
    public void checkDelete(String file) {
        throw new SecurityException("checkDelete权限异常"+file);
    }

    //检查程序是否连接网络
    @Override
    public void checkConnect(String host, int port) {
        throw new SecurityException("checkConnect权限异常"+host+":"+port);
    }
}
