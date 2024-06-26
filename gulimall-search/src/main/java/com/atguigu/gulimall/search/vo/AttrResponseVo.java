package com.atguigu.gulimall.search.vo;

import lombok.Data;

/**
 * @ClassName AttrResponseVo
 * @Description
 * @Author Richard
 * @Date 2024-04-04 22:46
 **/
@Data
public class AttrResponseVo {
    private Long attrId;
    /**
     * 属性名
     */
    private String attrName;
    /**
     * 是否需要检索[0-不需要，1-需要]
     */
    private Integer searchType;
    /**
     * 属性图标
     */
    private String icon;

    //0-单选，1多选
    private Integer valueType;

    /**
     * 可选值列表[用逗号分隔]
     */
    private String valueSelect;
    /**
     * 属性类型[0-销售属性，1-基本属性，2-既是销售属性又是基本属性]
     */
    private Integer attrType;
    /**
     * 启用状态[0 - 禁用，1 - 启用]
     */
    private Long enable;
    /**
     * 所属分类
     */
    private Long catelogId;
    /**
     * 快速展示【是否展示在介绍上；0-否 1-是】，在sku中仍然可以调整
     */
    private Integer showDesc;

    //属性分组ID
    private Long attrGroupId;

    private String catelogName;

    private String groupName;

    private Long [] catelogPath;
}
