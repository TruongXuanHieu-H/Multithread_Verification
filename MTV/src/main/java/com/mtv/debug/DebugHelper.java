package com.mtv.debug;


public class DebugHelper {
    public static void print(String content, StackTraceElement stack) {
        System.out.println(content + "\u001B[37m"    //low-visibality
                + "\t :: ["+stack+"]"  //code path
                + "\u001B[0m ");
    }

    public static void print(String content) {
        StackTraceElement stack = new Throwable().getStackTrace()[1];
        System.out.println(content + "\u001B[37m"    //low-visibility
                + "\t :: ["+stack+"]"  //code path
                + "\u001B[0m ");
    }
}
