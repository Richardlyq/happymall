package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName Catelog2Vo
 * @Description
 * @Author Richard
 * @Date 2024-03-25 16:33
 **/
//2级分类vo
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catelog2Vo implements Serializable {
    private String catalog1Id; //一级父分类
    private List<Catelog3Vo> catalog3List; //三级子分类
    private String id;
    private String name;

    //3级分类vo
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Catelog3Vo implements Serializable{
        private String catalog2Id; //父分类，2级分类id
        private String id;
        private String name;
    }
}
