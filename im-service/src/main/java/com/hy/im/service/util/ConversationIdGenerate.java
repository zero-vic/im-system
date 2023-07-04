package com.hy.im.service.util;

/**
 * @ClassName ConversationIdGenerate
 * description:
 * yao create 2023年07月04日
 * version: 1.0
 */
public class ConversationIdGenerate {
    //A|B
    //B A

    /**
     * 根据fromId和toId生成新的id
     * @param fromId
     * @param toId
     * @return
     */
    public static String generateP2PId(String fromId,String toId){
        int i = fromId.compareTo(toId);
        if(i < 0){
            return toId+"|"+fromId;
        }else if(i > 0){
            return fromId+"|"+toId;
        }

        throw new RuntimeException("");
    }
}
