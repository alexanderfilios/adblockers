package com.adblockers.parser;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
@FunctionalInterface
public interface ConverterInterface<S, T> {
    T convert(S source);
}
