package com.atguigu.common.to.es;

import lombok.Data;

/**
 * @ClassName SkusHasStock
 * @Description
 * @Author Richard
 * @Date 2024-03-18 21:33
 **/
@Data
public class SkuHasStockVo {
    private Long skuId;
    private Boolean hasStock;
}
