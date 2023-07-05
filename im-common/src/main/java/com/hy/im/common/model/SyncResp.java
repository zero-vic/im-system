package com.hy.im.common.model;

import lombok.Data;

import java.util.List;

/**
 * 增量同步返回体
 * @param <T>
 */
@Data
public class SyncResp<T> {
    /**
     * 最大的seq
     */
    private Long maxSequence;
    /**
     * 是否拉取完
     */
    private boolean isCompleted;
    /**
     * 数据列表
     */
    private List<T> dataList;

}