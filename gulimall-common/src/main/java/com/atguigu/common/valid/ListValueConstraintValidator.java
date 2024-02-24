package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @ClassName ListValueConstraintValidator
 * @Description
 * @Author Richard
 * @Date 2024-02-24 23:24
 **/

public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer>{

    private Set<Integer> set = new HashSet<>();

    //初始化方法
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.value();
        for (int val : vals) {
            set.add(val);
        }
    }

    //判断是否校验成功
    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {

        return set.contains(integer);
    }
}
