package com.adblockers.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
public final class Utilities {
    public Utilities() {}

    public static <T1, T2, R> Function<T1, Stream<R>> crossWith(
            Supplier<? extends Stream<T2>> otherSupplier,
            BiFunction<? super T1, ? super T2, ? extends R> combiner) {
        return t1 -> otherSupplier.get().map(t2 -> combiner.apply(t1, t2));
    }

    public static <T> List<T> subList(List<T> list, Predicate<T> startCondition, Predicate<T> endCondition) {
        boolean started = false;
        List<T> trimmedList = new LinkedList<T>();
        for (T current : list) {
            // Start storing
            if (!started && startCondition.test(current)) {
                started = true;
            }
            // Store
            if (started) {
                trimmedList.add(current);
            }
            // Exit the loop
            if (started && endCondition.test(current)) {
                return trimmedList;
            }
        }
        // If the endCondition never evaluated to true
        return trimmedList;
    }
}
