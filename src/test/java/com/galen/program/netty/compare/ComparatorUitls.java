package com.galen.program.netty.compare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by baogen.zhang on 2018/10/25
 *
 * @author baogen.zhang
 * @date 2018/10/25
 */
public class ComparatorUitls {
    private static Logger logger = LoggerFactory.getLogger(ComparatorUitls.class);

    public static <T> List<T> parallelSort(List<T> a, Comparator<? super T> cmp) {
        int nThreads = Runtime.getRuntime().availableProcessors();
        final ExecutorService executorService = new ThreadPoolExecutor(nThreads, nThreads,
                8L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(nThreads),new ThreadPoolExecutor.CallerRunsPolicy());
        final CompletionService completionService = new ExecutorCompletionService(executorService);
        int size = (int) Math.ceil(a.size() / nThreads);
        Future<List<T>> future = parallelSort(executorService,size,a,cmp);
        try {
            return future.get();
        } catch (InterruptedException e) {
            logger.error("中断异常",e);
        } catch (ExecutionException e) {
            logger.error("排序代码异常",e);
        }finally {
            executorService.shutdown();
        }
        return null;
    }

    private static <T> Future<List<T>> parallelSort(final ExecutorService executorService,final Integer size, final List<T> a, final Comparator<? super T> cmp) {
        Future<List<T>> future = executorService.submit(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                if (size >= a.size() || 8 > a.size()) {
                    Collections.sort(a, cmp);
                    return a;
                } else {
                    int mid = (int) Math.floor(a.size() / 2);
                    List<T> left = parallelSort(executorService, size, a.subList(0, mid), cmp).get();
                    List<T> right = parallelSort(executorService, size, a.subList(mid, a.size()), cmp).get();
                    List<T> result = new ArrayList<T>(a.size());
                    for (int i = 0, j = 0; i < left.size() || j < right.size(); ) {
                        int r = 0;
                        if (i >= left.size()) {
                            r = 1;
                        } else if (j >= right.size()) {
                            r = -1;
                        } else {
                            r = cmp.compare(left.get(i), right.get(j));
                        }

                        if (r < 0) {
                            result.add(left.get(i));
                            i++;
                        } else if (r > 0) {
                            result.add(right.get(j));
                            j++;
                        } else {
                            result.add(left.get(i));
                            i++;
                            result.add(right.get(j));
                            j++;
                        }
                    }
                    return result;
                }
            }
        });
        return future;

    }

    public static abstract class BaseComparator<T> extends AbstractComparator<T> {
        @Override
        public int compare(T o1, T o2) {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }
            return compareNotNull(o1, o2);
        }

        public abstract int compareNotNull(T o1, T o2);

    }

    public static <T> Comparator<T> trans(final java.util.Comparator<? super T> comparator) {
        return new AbstractComparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return comparator.compare(o1, o2);
            }
        };
    }

    /**
     * 把比较对象转为int再比较
     *
     * @param toInt 把
     * @return
     */
    public static <T> Comparator<T> compareInt(final ToInt<T> toInt) {
        return new BaseComparator<T>() {
            @Override
            public int compareNotNull(T o1, T o2) {
                return toInt.toInt(o1) - toInt.toInt(o2);
            }
        };
    }

    /**
     * 把比较对象转为Long再比较
     *
     * @param toLong
     * @return
     */
    public static <T> Comparator<T> compareLong(final ToLong<T> toLong) {
        return new BaseComparator<T>() {
            @Override
            public int compareNotNull(T o1, T o2) {
                return toLong.toLong(o1).compareTo(toLong.toLong(o2));
            }
        };
    }

    /**
     * 把比较对象转为Double再比较
     *
     * @param toDouble
     * @return
     */
    public static <T> Comparator<T> compareDouble(final ToDouble<T> toDouble) {
        return new BaseComparator<T>() {
            @Override
            public int compareNotNull(T o1, T o2) {
                return toDouble.toDouble(o1).compareTo(toDouble.toDouble(o2));
            }
        };
    }

    /**
     * 把比较对象转为String再比较
     *
     * @param toString
     * @return
     */
    public static <T> Comparator<T> compareString(final ToString<T> toString) {
        return new BaseComparator<T>() {
            @Override
            public int compareNotNull(T o1, T o2) {
                return toString.toString(o1).compareTo(toString.toString(o2));
            }
        };
    }

    public static <T> Comparator<T> compareIntField(String fieldName) {
        return compareInt(new ToIntField<T>(fieldName));
    }

    public static <T> Comparator<T> compareDoubleField(String fieldName) {
        return compareDouble(new ToDoubleField<T>(fieldName));
    }

    public static <T> Comparator<T> comparLongField(String fieldName) {
        return compareLong(new ToLongField<T>(fieldName));
    }

    public static <T> Comparator<T> compareStringField(String fieldName) {
        return compareString(new ToStringField<T>(fieldName));
    }

    public static class ToIntField<T> implements ToInt<T> {
        private String fieldName;

        public ToIntField(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public int toInt(T t) {
            return getField(t, fieldName);
        }
    }

    public static class ToLongField<T> implements ToLong<T> {
        private String fieldName;

        public ToLongField(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public Long toLong(T t) {
            return getField(t, fieldName);
        }

    }

    public static class ToDoubleField<T> implements ToDouble<T> {
        private String fieldName;

        public ToDoubleField(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public Double toDouble(T t) {
            return getField(t, fieldName);
        }

    }

    public static class ToStringField<T> implements ToString<T> {
        private String fieldName;

        public ToStringField(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public String toString(T t) {
            return getField(t, fieldName);
        }
    }

    private static <T> T getField(Object t, String fieldName) {
        Field field = ReflectionUtils.findField(t.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        try {
            return (T) ReflectionUtils.getField(field, t);
        } catch (Exception e) {
            throw new RuntimeException(fieldName + "字段类型不匹配！");
        }
    }


    interface ToInt<T>{
        int toInt(T t);
    }

    interface ToLong<T>{
        Long toLong(T t);
    }

    interface ToDouble<T>{
        Double toDouble(T t);
    }

    interface ToString<T>{
        String toString(T t);
    }
}
