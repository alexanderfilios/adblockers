package com.adblockers.controllers;

import com.adblockers.entities.Metric;
import com.adblockers.entities.MetricAdapter;
import com.adblockers.repos.MetricAdapterRepository;
import com.adblockers.repos.MetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 20/10/16.
 */
@CrossOrigin
@RestController
@RequestMapping("adapters/")
public class AdapterController {

    private MetricAdapterRepository metricAdapterRepository;
    private MetricRepository metricRepository;

    @RequestMapping(value = {"metrics"}, method = RequestMethod.PUT)
    public void convertMetrics() {
        List<Metric> metrics = this.metricAdapterRepository.findAll().stream()
                .map(MetricAdapter::toMetric)
                .filter(metric -> !metric.getMetricType().equals(Metric.MetricType.UNDEFINED))
                .collect(Collectors.toList());

        this.metricRepository.deleteAll();
        this.metricRepository.save(metrics);
    }

    @Autowired
    public void setMetricAdapterRepository(MetricAdapterRepository metricAdapterRepository) { this.metricAdapterRepository = metricAdapterRepository; }

    @Autowired
    public void setMetricRepository(MetricRepository metricRepository) { this.metricRepository = metricRepository; }
}
