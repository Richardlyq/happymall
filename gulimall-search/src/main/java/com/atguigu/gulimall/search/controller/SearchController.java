package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName SearchController
 * @Description
 * @Author Richard
 * @Date 2024-04-02 23:08
 **/

@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request){
        searchParam.set_queryString(request.getQueryString());
        //根据参数去es中检索商品
        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);
        return "index";
    }
}
