package com.atguigu.gulimall.product.service.impl;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    //查出商品的所有分类，并用树形结构封装起来
    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);
        //用树形结构封装起来
        List<CategoryEntity> menuList = categoryEntityList.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(menu -> {
                    menu.setChildren(getChildren(menu, categoryEntityList));
                    return menu;
                })
                .sorted((menu1,menu2)->{
                    return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
                })
                .collect(Collectors.toList());
        return menuList;
    }

    @Override
    public void removeMenusByIds(List<Long> asList) {
        //TODO: 检查当前的菜单是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    //通过attrGroupId找到其对应父分类数组
    @Override
    public Long[] findParentCatelogPath(Long catelogId) {
        ArrayList<Long> list = new ArrayList<>();
        ArrayList<Long> catelogPath = findCatelogPath(catelogId, list);
        Collections.reverse(catelogPath);
        return catelogPath.toArray(new Long[catelogPath.size()]);
    }


//    @Caching(evict = {   //批量操作，同时清除两个数据
//            @CacheEvict(value = "category", key = "'getLevel1Categorys'"),
//            @CacheEvict(value = "category", key = "'getCatalogJson'")
//    })
//    @CacheEvict(value = "category", allEntries = true)// 失效模式（修改后删除缓存），删除category分区下所有的数据
//    @CachePut() //双写模式
    @Transactional
    //级联修改，不仅修改本表，还要修改关联表，以保持数据同步
    @Override
    public void updateCascade(CategoryEntity category) {
        //修改自身的表
        this.updateById(category);
        //修改级联的表
        if (!StringUtils.isEmpty(category.getName())){
            categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
        }
    }

    @Cacheable(value = "category", key = "#root.method.name") //key里面 " " 里面默认是表达式，写字符串的话，需要" " 里面再加 '',即"' '"
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("执行业务，开始查询一级分类");
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        System.out.println("执行业务， 查询三级分类数据");
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);
        //得到所有的一级分类
        List<CategoryEntity> level1Categorys = getParentCid(categoryEntityList,0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //得到所有的二级分类
            List<CategoryEntity> categoryLevel2 = getParentCid(categoryEntityList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryLevel2 != null){
                //封装Catelog2Vo数据
                catelog2Vos = categoryLevel2.stream().map(level2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, level2.getCatId().toString(), level2.getName());
                    //得到二级分类下对应的三级分类
                    List<CategoryEntity> categoryLevel3 = getParentCid(categoryEntityList, level2.getCatId());
                    List<Catelog2Vo.Catelog3Vo> catelog3Vos = null;
                    if (categoryLevel3 != null){
                        //封装三级分类的数据
                        catelog3Vos = categoryLevel3.stream().map(level3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(level2.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                    }
                    //设置二级分类下的三级分类数据
                    catelog2Vo.setCatalog3List(catelog3Vos);
                    return catelog2Vo;
                }).collect(Collectors.toList());

            }
            return catelog2Vos;
        }));
        return parent_cid;
    }

    // TODO: 产生堆外内存溢出：OutOfDirectMemoryError
    //1）、springboot2.0以后默认使用lettuce作为操作redis的客户端，它使用netty进行网络通信
    //2）、lettuce的bug导致netty堆外内存溢出 -Xmx300m; netty如果没有指定堆外内存，默认使用-Xmx300m
    // 解决方案不能使用-Dio.netty.maxDirectMemory调大堆外内存，这样只能延缓堆外内存溢出而已，不能根本解决
    // 真正的解决方案：1、升级lettuce客户端 ； 2） 切换使用jedis
    // lettuce、jedis操作redis的底层客户端，spring再次封装这两者只用redisTemplate操作redis即可
//    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson2() {

        //1.空结果缓存：解决缓存穿透
        //2.设置过期时间（加随机值）：解决缓存雪崩
        //3.加锁：解决缓存击穿

        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        // redis中的值需要放入json字符串，因为json跨语言，跨平台兼容
        if (StringUtils.isEmpty(catalogJson)){
            //1.缓存中没有数据，就查询数据库
            Map<String, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithRedisLock();
//            //2.将查到的数据转换成json后放入缓存中
//            String jsonString = JSON.toJSONString(catalogJsonFromDB);
//            stringRedisTemplate.opsForValue().set("catalogJson",jsonString);
            return catalogJsonFromDB;
        }
        //如果缓存中有数据，则将json转换成需要的对象返回
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;
    }
    
    /*
     * @description: 缓存里的数据如何与数据库的数据保持一致？
     * 缓存数据一致性： （1）双写模式，更新完数据库，再去更新缓存 （2）失效模式，更新完数据库之后，直接删掉缓存的数据
     * @author: liyuqi 
     * @date:  22:48 2024/3/31
     * @param:
     * @return:
     **/
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedissonLock(){
        //1、占分布式锁。去redis占坑
        //（锁的粒度，越细越快:具体缓存的是某个数据，11号商品） product-11-lock
        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDb = null;
        try {
            dataFromDb = getDataFromDb();
        }finally {
            lock.unlock();
        }
        return dataFromDb;
    }

    /*
     * @description: 从数据库查询并封装数据：： 分布式锁
     *                  加分布式锁的关键： （1）一定要保证加锁的操作是原子性的 （2）一定要保证删除锁的操作是原子性的
     * @author: liyuqi 
     * @date:  16:13 2024/3/30
     * @param: []
     * @return: java.util.Map<java.lang.String,java.util.List<com.atguigu.gulimall.product.vo.Catelog2Vo>>
     **/
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedisLock(){
        //1.占分布式锁，去redis占坑
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock){
            //加锁成功，执行业务
            System.out.println("获取分布式锁成功");
            Map<String, List<Catelog2Vo>> dataFromDb = null;
            try{
                 dataFromDb = getDataFromDb();
            }finally {
                //删除锁，用lua脚本删除锁的目的是为了让删除锁的操作是原子性的
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return dataFromDb;
        }else {
            System.out.println("获取分布式锁失败...等待重试...");
            //加锁失败...重试机制
            //休眠一百毫秒
            try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
            return getCatalogJsonFromDBWithRedisLock();     //自旋的方式
        }

    }

    /*
     * @description: 从数据库查询并封装数据::本地锁
     * @author: liyuqi
     * @date:  16:14 2024/3/30
     * @param: []
     * @return: java.util.Map<java.lang.String,java.util.List<com.atguigu.gulimall.product.vo.Catelog2Vo>>
     **/
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithLocalLock() {

        //TODO:本地锁，synchronized，JUC(lock)，在分布式情况下，想要锁住所有，必须使用分布式锁
        //只要是同一把锁，就能锁住这个锁的所有线程，synchronized (this)：SpringBoot所有的组件在容器中都是单例的。
        synchronized (this){
            return getDataFromDb();
        }

    }

    //从数据库中获取三级分类数据
    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        // redis中的值需要放入json字符串，因为json跨语言，跨平台兼容
        if (!StringUtils.isEmpty(catalogJson)){
            //如果缓存不为null，直接返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);
        //得到所有的一级分类
        List<CategoryEntity> level1Categorys = getParentCid(categoryEntityList,0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //得到所有的二级分类
            List<CategoryEntity> categoryLevel2 = getParentCid(categoryEntityList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryLevel2 != null){
                //封装Catelog2Vo数据
                catelog2Vos = categoryLevel2.stream().map(level2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, level2.getCatId().toString(), level2.getName());
                    //得到二级分类下对应的三级分类
                    List<CategoryEntity> categoryLevel3 = getParentCid(categoryEntityList, level2.getCatId());
                    List<Catelog2Vo.Catelog3Vo> catelog3Vos = null;
                    if (categoryLevel3 != null){
                        //封装三级分类的数据
                        catelog3Vos = categoryLevel3.stream().map(level3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(level2.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                    }
                    //设置二级分类下的三级分类数据
                    catelog2Vo.setCatalog3List(catelog3Vos);
                    return catelog2Vo;
                }).collect(Collectors.toList());

            }
            return catelog2Vos;
        }));
        //将查到的数据序列化之后放入缓存中
        String jsonString = JSON.toJSONString(parent_cid);
        stringRedisTemplate.opsForValue().set("catalogJson",jsonString,1, TimeUnit.DAYS);
        return parent_cid;
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> categoryEntityList, Long parent_cid) {
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        List<CategoryEntity> collect = categoryEntityList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
    }

    //递归调用得到父分类路径 225, 34, 2
    private ArrayList<Long> findCatelogPath(Long catelogId, ArrayList<Long> list) {
        list.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid() != 0){
            findCatelogPath(byId.getParentCid(),list);
        }
        return list;
    }


    //递归查找当前菜单的子菜单
    private List<CategoryEntity> getChildren(CategoryEntity currentMenu, List<CategoryEntity> allMenu){
        List<CategoryEntity> childrenMenu = allMenu.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == currentMenu.getCatId())
                .map(menu -> {
                    menu.setChildren(getChildren(menu, allMenu));
                    return menu;
                })
                .sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                })
                .collect(Collectors.toList());
        return childrenMenu;

    }



}