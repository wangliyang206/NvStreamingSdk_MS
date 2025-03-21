package com.meishe.sdkdemo.capture;

/**
 * @author jml
 * @des 美型的类别对应的适配
 */
public class BeautyShapeDataKindItem {
    /**
     * 美型类型的名字
     * beauty name
     */
    private String name;
    /**
     * 对应的
     * icon
     */
    private int icon;
    /**
     * 类型
     * 目前分为两个类型
     * 1.美型1，使用之前版本中的美型数据
     * 2.美型2，新建的一套默认值系统
     *
     * Beauty has two type
     *Beauty 1, using US data from previous versions
     *Beauty 2, A new set of default value system
     */
    private int type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * 美型的策略类型 等同于strategy
     * The strategy type of American style is equivalent to strategy
     */
    class Type{
        /**
         * 旧有的默认值体系,
         * Old default value system
         */
        public static final int NORMAL = 0;
        /**
         * 新建的默认体系
         * New default value system
         */
        public static final int NEW_BUILD = 1;
    }
}
