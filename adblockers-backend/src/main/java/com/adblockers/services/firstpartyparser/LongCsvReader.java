package com.adblockers.services.firstpartyparser;

import au.com.bytecode.opencsv.CSVReader;
import com.adblockers.AdblockersBackendApplication;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
@Service
public class LongCsvReader<T> implements LongCsvReaderService<T> {
    private static final Logger LOGGER = Logger.getLogger(LongCsvReader.class);

    private Function<T, T> objectFormatter;
    private Predicate<T> filter;
    private Long min;
    private Long max;
    private Consumer<T> action;
    private Function<String[], T> lineParser;

    public Collection<T> read(String fileName) {
        String filePath = AdblockersBackendApplication.RESOURCES_PATH + fileName + ".csv";
        LOGGER.info("Parsing file " + filePath);

        List<T> result = new LinkedList<T>();
        Date start = new Date();
        try (CSVReader csvReader = new CSVReader(new FileReader(filePath))) {
            String[] nextLine;
            Long lineCounter;

            for (nextLine = csvReader.readNext(), lineCounter = 0L;
                 nextLine != null && (max == null || lineCounter < max);
                 nextLine = csvReader.readNext(), lineCounter++) {

                if (min != null && lineCounter < min) {
                    continue;
                }
                T o = lineParser.apply(nextLine);
                o = objectFormatter.apply(o);
                if (filter.test(o)) {
                    action.accept(o);
                    result.add(o);
                }
            }

        } catch (IOException e) {
            LOGGER.error("CSV file parsing failed", e);
        }
        LOGGER.info("Read " + result.size() + " elements. Time elapsed: " + (new Date().getTime() - start.getTime()) + "msec");
        return result;
    }

    public LongCsvReader() {
        this.objectFormatter = input -> input;
        this.filter = input -> true;
        this.action = input -> {};
        this.lineParser = input -> null;
    }

    public void setObjectFormatter(Function<T, T> objectFormatter) {
        this.objectFormatter = objectFormatter;
    }
    public void setFilter(Predicate<T> filter) {
        this.filter = filter;
    }
    public void setAction(Consumer<T> action) {
        this.action = action;
    }
    public void setLineParser(Function<String[], T> lineParser) {
        this.lineParser = lineParser;
    }
}
