package com.bindada.unsafecode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class WriteFileError {
    public static void main(String[] args) throws IOException {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "src/main/resources/木马程序.bat";
        String errorCode = "java -version 2>&1";
        Files.write(Paths.get(filePath), Arrays.asList(errorCode));
        System.out.println("木马写入成功，哈哈哈");
    }
}
