package com.adblockers.utils;

import com.sun.istack.internal.Nullable;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
@FunctionalInterface
public interface ConverterInterface<S, T> {
    T convert(@Nullable S source);
}
