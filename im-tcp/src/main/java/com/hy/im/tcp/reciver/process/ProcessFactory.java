package com.hy.im.tcp.reciver.process;

/**
 * @ClassName ProcessFactory
 * description: 消息处理工厂
 * yao create 2023年07月03日
 * version: 1.0
 */
public class ProcessFactory {

    private static BaseProcess defaultProcess;

    static{
        defaultProcess = new BaseProcess() {
            @Override
            public void processBefore() {

            }

            @Override
            public void processAfter() {

            }
        };
    }

    public static BaseProcess getMassageProcess(Integer command){
        return defaultProcess;
    }
}
