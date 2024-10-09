package com.bindada.unsafecode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ReadFileError {
    public static void main(String[] args) throws IOException {
        String userDir = System.getProperty("user.dir");
        String filepath = userDir + File.separator + "src/main/resources/application.yml";
        List<String> allLines = Files.readAllLines(Paths.get(filepath));
        System.out.println(allLines);
    }
}
