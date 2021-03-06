package com.alibaba.otter.shared.arbitrate.impl.setl.lb;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter.LoadBanlanceAlgorithm;

/**
 * 获取next node
 * 
 * @author jianghang 2013-2-28 下午09:55:56
 * @version 4.1.4
 */
public class LoadBalanceFactory {

    public static Node getNextExtractNode(Long pipelineId) throws InterruptedException {
        Pipeline pipeline = ArbitrateConfigUtils.getPipeline(pipelineId);
        LoadBanlanceAlgorithm loadBanlanceAlgorithm = pipeline.getParameters().getLbAlgorithm();
        LoadBalance loadbalance = null;
        if (loadBanlanceAlgorithm.isRandom()) {
            loadbalance = ArbitrateFactory.getInstance(pipelineId, ExtractRandomLoadBanlance.class);
        } else if (loadBanlanceAlgorithm.isRoundRbin()) {
            loadbalance = ArbitrateFactory.getInstance(pipelineId, ExtractRoundRobinLoadBalance.class);
        } else {
            loadbalance = ArbitrateFactory.getInstance(pipelineId, ExtractStickLoadBalance.class);
        }
        Node node = loadbalance.next();// 获取下一个处理节点信息
        return node;
    }

    public static Node getNextTransformNode(Long pipelineId) throws InterruptedException {
        Pipeline pipeline = ArbitrateConfigUtils.getPipeline(pipelineId);
        LoadBanlanceAlgorithm loadBanlanceAlgorithm = pipeline.getParameters().getLbAlgorithm();
        LoadBalance loadbalance = null;
        if (loadBanlanceAlgorithm.isRandom()) {
            loadbalance = ArbitrateFactory.getInstance(pipelineId, TransformRandomLoadBanlance.class);
        } else if (loadBanlanceAlgorithm.isRoundRbin()) {
            loadbalance = ArbitrateFactory.getInstance(pipelineId, TransformRoundRobinLoadBalance.class);
        } else {
            loadbalance = ArbitrateFactory.getInstance(pipelineId, TransformStickLoadBalance.class);
        }
        Node node = loadbalance.next();// 获取下一个处理节点信息
        return node;
    }

}
