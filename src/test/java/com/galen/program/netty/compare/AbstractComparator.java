package com.galen.program.netty.compare;

import java.util.List;

/**
 * Created by baogen.zhang on 2018/10/24
 *
 * @author baogen.zhang
 * @date 2018/10/24
 */
public abstract class AbstractComparator<T> implements Comparator<T> {

    @Override
    public Comparator<T> reverse() {
        return new AbstractComparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return AbstractComparator.this.compare(o2, o1);
            }
        };
    }

    @Override
    public Comparator<T> then(final Comparator<? super T> comparator) {
        if (comparator == null) {
            return this;
        }
        return new AbstractComparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                int i = AbstractComparator.this.compare(o1, o2);
                return i != 0 ? i : comparator.compare(o1, o2);
            }
        };
    }

    @Override
    public Comparator<T> then(List<Comparator<T>> comparators) {
        if (comparators == null || comparators.size() == 0) {
            return this;
        }
        return then(comparators.remove(0)).then(comparators);
    }

    @Override
    public Comparator<T> thenSelect(final Comparator<? super T> select, final Comparator<? super T> less, final Comparator<? super T> equal, final Comparator<? super T> greater) {
        if (select == null || less == null || equal == null || greater == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        return then(new AbstractComparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                int i = select.compare(o1, o2);
                if (i < 0) {
                    return less.compare(o1, o2);
                } else if (i > 0) {
                    return greater.compare(o1, o2);
                } else {
                    return equal.compare(o1, o2);
                }
            }
        });
    }

}
