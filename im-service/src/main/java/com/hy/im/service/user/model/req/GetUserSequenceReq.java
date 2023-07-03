package com.hy.im.service.user.model.req;


import com.hy.im.common.model.RequestBase;
import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class GetUserSequenceReq extends RequestBase {

    private String userId;

}
