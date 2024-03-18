package com.atguigu.gulimall.search;


import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.mysql.cj.QueryBindings;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {
    @Autowired
    private RestHighLevelClient client;

    /*
     * @description: 测试在es中复杂查询
     * @author: liyuqi
     * @date:  21:20 2024/3/17
     * @param:
     * @return:
     **/
    @Test
    public void searchData() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        //指定要查询的索引
        searchRequest.indices("users");
        //指定DSL，检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //匹配
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //聚合
//        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
//        searchSourceBuilder.aggregation(balanceAvg);
        System.out.println(searchSourceBuilder.toString()); // 打印检索条件

        searchRequest.source(searchSourceBuilder);
        //2.执行检索,得到返回结果searchResponse
        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        //3.分析结果 searchResponse
        System.out.println("searchResponse = " + searchResponse); //打印出JSON字符串
        SearchHits hits = searchResponse.getHits(); //最外面的一层hits
        SearchHit[] searchHits = hits.getHits(); //得到真正的数据
        for (SearchHit searchHit : searchHits) {
            String string = searchHit.getSourceAsString();// 得到真实数据的JSON字符串，再用Bean封装
            User user = JSON.parseObject(string, User.class);
            System.out.println("user = " + user);
        }
    }

    /*
     * @description: 测试保存数据到es中
     * @author: liyuqi
     * @date:  21:10 2024/3/17
     * @param: []
     * @return: void
     **/
    @Test
    public void indexData() throws IOException {
        IndexRequest request = new IndexRequest("users");
        request.id("1");
        User user = new User();
        user.setUserName("lisi");
        user.setGender("男");
        user.setAge(24);
        String jsonString = JSON.toJSONString(user);
        //要保存的内容
        request.source(jsonString, XContentType.JSON);
        //执行操作
        IndexResponse response = client.index(request, GulimallElasticSearchConfig.COMMON_OPTIONS);
        //提取有用的响应数据
        System.out.println("response = " + response);
    }

    @Data
    class User{
        private String userName;
        private String gender;
        private Integer age;
    }

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

}
