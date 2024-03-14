package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @ClassName PurchaseItemVo
 * @Description
 * @Author Richard
 * @Date 2024-03-13 22:54
 **/
@Data
public class PurchaseItemDoneVo {
    //{itemId:1,status:4,reason:""}
    private Long itemId;
    private Integer status;
    private String reason;
}
