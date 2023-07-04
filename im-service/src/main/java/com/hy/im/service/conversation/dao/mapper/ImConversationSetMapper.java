package com.hy.im.service.conversation.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hy.im.service.conversation.dao.ImConversationSetEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;



@Mapper
public interface ImConversationSetMapper extends BaseMapper<ImConversationSetEntity> {
    /**
     * 更新readedSequence
     * @param imConversationSetEntity
     */
    @Update(" update im_conversation_set set readed_sequence = #{readedSequence},sequence = #{sequence} " +
    " where conversation_id = #{conversationId} and app_id = #{appId} AND readed_sequence < #{readedSequence}")
    public void readMark(ImConversationSetEntity imConversationSetEntity);

    /**
     * 查询最大的sequence
     * @param appId
     * @param userId
     * @return
     */
    @Select(" select max(sequence) from im_conversation_set where app_id = #{appId} AND from_id = #{userId} ")
    Long geConversationSetMaxSeq(Integer appId, String userId);
}
