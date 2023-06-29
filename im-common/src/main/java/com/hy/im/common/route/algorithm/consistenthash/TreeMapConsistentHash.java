package com.hy.im.common.route.algorithm.consistenthash;

import com.hy.im.common.enums.UserErrorCode;
import com.hy.im.common.exception.ApplicationException;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @ClassName TreeMapConsistentHash
 * description: TreeMap实现一致性hash算法
 * yao create 2023年06月29日
 * version: 1.0
 */
public class TreeMapConsistentHash extends AbstractConsistentHash{

    private TreeMap<Long,String> treeMap = new TreeMap<Long, String>();

    private static final int node_size = 2;



    @Override
    protected void add(long key, String value) {
        // 增加虚拟节点 , 解决负载分布不均的问题
        for (int i = 0;i<node_size;i++){
            treeMap.put(super.hash("node"+key+i),value);
        }
        treeMap.put(key,value);
    }

    @Override
    protected String getFirstNodeValue(String value) {
        Long hash = super.hash(value);
        SortedMap<Long, String> tailMap = treeMap.tailMap(hash);
        if(!tailMap.isEmpty()){
            return tailMap.get(tailMap.firstKey());
        }
        if(treeMap.size() == 0){
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        return treeMap.firstEntry().getValue();
    }

    @Override
    protected void processBefore() {
        treeMap.clear();
    }
}
