package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.vo.spuadd.Attr;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 **/

@Data
@ToString
public class SpuItemAttrGroupVo {

    private String groupName;

    private List<Attr> attrs;

}
