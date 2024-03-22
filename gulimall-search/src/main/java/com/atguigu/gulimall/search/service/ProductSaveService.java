package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName ProductSaveService
 * @Description
 * @Author Richard
 * @Date 2024-03-19 12:31
 **/


public interface ProductSaveService {

    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
