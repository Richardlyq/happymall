package com.atguigu.common.constant;

/**
 * @ClassName ProductConstant
 * @Description
 * @Author Richard
 * @Date 2024-02-26 23:36
 **/

public class ProductConstant {
    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"基本属性"), ATTR_TYPE_SALE(0,"销售属性");
        private int code;
        private String msg;

        AttrEnum(int code, String msg){
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum StatusEnum{
        NEW_SPU(0,"新建状态"), NEW_UP(1,"商品上架"),NEW_DOWN(2,"商品下架");
        private int code;
        private String msg;

        StatusEnum(int code, String msg){
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
