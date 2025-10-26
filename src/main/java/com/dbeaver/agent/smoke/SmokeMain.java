package com.dbeaver.agent.smoke;

public class SmokeMain {
    public static void main(String[] args) throws Exception {
        System.out.println("SmokeMain started");
        // Keep JVM alive briefly to observe agent behavior
        Thread.sleep(2000);
        System.out.println("SmokeMain finished");
    }
}

