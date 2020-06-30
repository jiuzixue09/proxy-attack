package com.bigbigwork.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExecuteUtil {
    private static final String PREFIX = "cmd /c ";

    public static String exec(String command){
        command = PREFIX + command;
        StringBuilder sb = new StringBuilder();

        try {
            Process child = Runtime.getRuntime().exec(command);

            try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(child.getInputStream()))){
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    sb.append(line).append("\n");
                }

            }
            child.waitFor();
        }catch (Exception e){
            sb.append(e.toString());
        }
        return sb.toString();
    }

    public static void execWithOutMessage(String command) throws IOException {
        command = PREFIX + command;
        Runtime.getRuntime().exec(command);
    }


}
