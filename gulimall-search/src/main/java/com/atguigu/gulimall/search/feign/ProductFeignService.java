package com.atguigu.gulimall.search.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @ClassName ProductFeignService
 * @Description
 * @Author Richard
 * @Date 2024-04-04 22:43
 **/
@FeignClient("gulimall-product")
public interface ProductFeignService {
    //远程调用商品服务，根据商品id获取商品名
    @GetMapping("product/attr/info/{attrId}")
    public R attrInfo(@PathVariable("attrId") Long attrId);
}
