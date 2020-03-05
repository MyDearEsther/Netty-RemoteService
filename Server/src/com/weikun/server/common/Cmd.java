package com.weikun.server.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


public class Cmd {

    public static String execCmd(String cmd,boolean isShell) throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        Runtime runtime = Runtime.getRuntime();
        Process process;
        if (isShell){
            process = runtime.exec(new String[]{"/bin/bash","-c",cmd});
        }else {
            process = runtime.exec(cmd);
        }
        BufferedReader bufferedReader = new BufferedReader
                (new InputStreamReader(process.getInputStream()));
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        if (!sb.toString().isEmpty()){
            LogUtil.e(sb.toString());
        }
        return sb.toString();
    }
}
