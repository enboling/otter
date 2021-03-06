package com.alibaba.otter.node.etl.common.jmx;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.otter.node.etl.common.jmx.StageAggregation.AggregationItem;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * 统计每个stage的运行信息
 * 
 * @author jianghang 2012-5-29 下午02:32:08
 * @version 4.0.2
 */
public class StageAggregationCollector {

    private Map<Long, Map<StageType, StageAggregation>> collector;
    private AtomicBoolean                               profiling = new AtomicBoolean(true);

    public StageAggregationCollector(){
        this(1024);
    }

    public StageAggregationCollector(final int bufferSize){
        collector = new MapMaker().makeComputingMap(new Function<Long, Map<StageType, StageAggregation>>() {

            public Map<StageType, StageAggregation> apply(Long input) {
                return new MapMaker().makeComputingMap(new Function<StageType, StageAggregation>() {

                    public StageAggregation apply(StageType input) {
                        return new StageAggregation(bufferSize);
                    }
                });
            }
        });
    }

    public void push(Long pipelineId, StageType stage, AggregationItem aggregationItem) {
        collector.get(pipelineId).get(stage).push(aggregationItem);
    }

    public String histogram(Long pipelineId, StageType stage) {
        return collector.get(pipelineId).get(stage).histogram();
    }

    public boolean isProfiling() {
        return profiling.get();
    }

    public void setProfiling(boolean profiling) {
        this.profiling.set(profiling);
    }

}
