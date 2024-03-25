package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    @Override
    public List<CategoryEntity> getLevel1Categorys() {

        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        //得到所有的一级分类
        List<CategoryEntity> level1Categorys = getLevel1Categorys();
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //得到所有的二级分类
            List<CategoryEntity> categoryLevel2 = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryLevel2 != null){
                //封装Catelog2Vo数据
                 catelog2Vos = categoryLevel2.stream().map(level2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, level2.getCatId().toString(), level2.getName());
                    //得到二级分类下对应的三级分类
                    List<CategoryEntity> categoryLevel3 = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", level2.getCatId()));
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