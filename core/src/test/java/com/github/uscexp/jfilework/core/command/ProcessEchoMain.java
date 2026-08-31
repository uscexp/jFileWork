package com.github.uscexp.jfilework.core.command;

public final class ProcessEchoMain {

    private ProcessEchoMain() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("hello-out");
        System.err.println("hello-err");
        if (args.length > 0) {
            Thread.sleep(Long.parseLong(args[0]));
        }
    }
}

