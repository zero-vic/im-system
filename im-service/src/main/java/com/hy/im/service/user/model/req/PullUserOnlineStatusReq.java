package com.hy.im.service.user.model.req;


import com.hy.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class PullUserOnlineStatusReq extends RequestBase {

    private List<String> userList;

}
