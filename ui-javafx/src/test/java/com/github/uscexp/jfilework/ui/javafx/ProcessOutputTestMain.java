package com.github.uscexp.jfilework.ui.javafx;

public final class ProcessOutputTestMain {

    private ProcessOutputTestMain() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("ui-test-out");
        System.err.println("ui-test-err");
        long sleepMillis = args.length > 0 ? Long.parseLong(args[0]) : 0L;
        if (sleepMillis > 0) {
            Thread.sleep(sleepMillis);
        }
        System.out.println("ui-test-done");
    }
}

