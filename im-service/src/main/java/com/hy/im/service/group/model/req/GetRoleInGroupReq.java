package com.hy.im.service.group.model.req;


import com.hy.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class GetRoleInGroupReq extends RequestBase {

    private String groupId;

    private List<String> memberId;
}
