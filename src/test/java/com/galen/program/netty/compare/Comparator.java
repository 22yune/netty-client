package com.galen.program.netty.compare;

import java.util.List;

/**
 * Created by baogen.zhang on 2018/10/24
 *
 * @author baogen.zhang
 * @date 2018/10/24
 */
public interface Comparator<T> extends java.util.Comparator<T> {

    /**
     * 反转比较结果
     *
     * @return
     */
    Comparator<T> reverse();

    /**
     * 如果相等就继续比较下一个
     *
     * @param comparator 下一个比较器
     * @return
     */
    Comparator<T> then(Comparator<? super T> comparator);

    /**
     * 如果相等就继续比较下一个的 多个连续的简写版本
     *
     * @param comparators 排好序的后续比较器
     * @return
     */
    Comparator<T> then(List<Comparator<T>> comparators);

    /**
     * 根据选择器的结果选择后续比较器
     *
     * @param select  选择器
     * @param less    选择器结果为小于时的后续比较器
     * @param equal   选择器结果为等于是的后续比较器
     * @param greater 选择器结果为大于时的后续比较器
     * @return
     */
    Comparator<T> thenSelect(Comparator<? super T> select, Comparator<? super T> less, Comparator<? super T> equal, Comparator<? super T> greater);


}
