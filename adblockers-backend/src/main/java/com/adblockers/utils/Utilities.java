package com.adblockers.utils;

import java.util.function.BiFunction;
import java.util.function.Function;
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
}
