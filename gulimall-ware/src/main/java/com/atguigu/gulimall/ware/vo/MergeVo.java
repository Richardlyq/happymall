package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName MergeVo
 * @Description
 * @Author Richard
 * @Date 2024-03-13 11:41
 **/
@Data
public class MergeVo {
    //purchaseId: 1, items: [1, 2]
    private Long purchaseId; //采购单
    private List<Long> items; //采购项
}
