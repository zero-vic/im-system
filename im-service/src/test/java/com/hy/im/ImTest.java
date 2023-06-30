package com.hy.im;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @ClassName ImTest
 * description:
 * yao create 2023年06月30日
 * version: 1.0
 */
@SpringBootTest
public class ImTest {

    private int id;

    @Before
    @Test
    public void test1(){
        id = 1;
    }
    @Test
    public void test2(){
        System.out.println("id:" + id);
    }
}
