package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * @ClassName AttrGroupRelationVo
 * @Description
 * @Author Richard
 * @Date 2024-02-28 10:57
 **/
@Data
public class AttrGroupRelationVo {
    //[{"attrId":1,"attrGroupId":2}]
    private Long attrId;
    private Long attrGroupId;
}
