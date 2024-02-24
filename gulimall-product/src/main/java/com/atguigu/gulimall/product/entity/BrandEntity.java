package com.atguigu.gulimall.product.entity;

import com.atguigu.common.group.AddGroup;
import com.atguigu.common.group.UpdateGroup;
import com.atguigu.common.group.UpdateStatusGroup;
import com.atguigu.common.valid.ListValue;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author Richard
 * @email 1409386193@qq.com
 * @date 2024-02-05 21:03:19
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改时必须指定品牌id", groups = {UpdateGroup.class})
	@Null(message = "新增时不能指定品牌id", groups = {AddGroup.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空", groups = {UpdateGroup.class, AddGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotEmpty(groups = {AddGroup.class})
	@URL(message = "品牌logo地址必须合法", groups = {UpdateGroup.class, AddGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
	@ListValue(value = {0,1},groups = {AddGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotEmpty(groups = {AddGroup.class})
	@Pattern(regexp="^[a-zA-Z]$", message = "首字母必须在a-z或A-Z之间",groups = {UpdateGroup.class, AddGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(groups = {AddGroup.class})
	@Min(value = 0, message = "排序必须大于0",groups = {UpdateGroup.class, AddGroup.class})
	private Integer sort;

}
