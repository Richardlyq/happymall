package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName PurchaseDoneVo
 * @Description
 * @Author Richard
 * @Date 2024-03-13 22:54
 **/
@Data
public class PurchaseDoneVo {
    private Long id;
    private List<PurchaseItemDoneVo> items;
}
