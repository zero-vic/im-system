package com.hy.im.service.group.model.req;


import com.hy.im.common.model.RequestBase;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class GetGroupReq extends RequestBase {

    private String groupId;

}
