package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

/**
 * @ClassName MallSearchService
 * @Description
 * @Author Richard
 * @Date 2024-04-03 12:22
 **/

public interface MallSearchService {
    SearchResult search(SearchParam searchParam);
}
