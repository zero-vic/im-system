package com.hy.im.service.user.model.req;


import com.hy.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;


@Data
public class GetUserInfoReq extends RequestBase {

    private List<String> userIds;


}
