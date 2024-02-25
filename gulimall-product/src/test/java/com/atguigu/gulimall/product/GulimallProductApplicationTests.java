package com.atguigu.gulimall.product;

//import com.aliyun.oss.OSSClient;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {


    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
//    private OSSClient ossClient;

    @Test
    public void testFindParentCatelogPath(){
        Long[] catelogPath = categoryService.findParentCatelogPath(225L);
        log.info("分类完整路径："+ Arrays.asList(catelogPath));
    }

    @Test
    void contextLoads() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(1L);
//        brandEntity.setName("华为");
//        brandService.save(brandEntity);

        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        list.forEach((item)->{
            System.out.println("item = " + item);
        });

        System.out.println("===============保存成功=======");
    }

//    @Test
//    public void testUpload() throws FileNotFoundException {
//        InputStream inputStream = new FileInputStream("C:\\Users\\Richard\\Desktop\\pai.jpg");
//        ossClient.putObject("gulimall-richard","pai.jpg",inputStream);
//        System.out.println("上传成功。。");
//    }

}
