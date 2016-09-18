package com.adblockers.services.firstpartyparser;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by alexandrosfilios on 18/09/16.
 * Reads a CSV-formatted file and extracts the first parties to be crawled
 */
public interface LongCsvReaderService<T> {
    void setObjectFormatter(ConverterInterface<T, T> objectFormatter);
    void setFilter(Predicate<T> filter);
    void setAction(Consumer<T> action);
    void setLineParser(ConverterInterface<String[], T> lineParser);

    Collection<T> read(String csvFileName);
}
