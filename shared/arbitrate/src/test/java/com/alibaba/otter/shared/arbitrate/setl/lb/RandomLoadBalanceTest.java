package com.alibaba.otter.shared.arbitrate.setl.lb;

import mockit.Mock;
import mockit.Mockit;

import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.lb.ExtractRandomLoadBanlance;
import com.alibaba.otter.shared.arbitrate.impl.setl.lb.TransformRandomLoadBanlance;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * Random lb测试
 * 
 * @author jianghang 2011-9-22 上午11:36:08
 * @version 4.0.0
 */
public class RandomLoadBalanceTest extends BaseLoadBalanceTest {

    @Test
    public void testExtract() {
        // 初始化节点
        Mockit.setUpMock(ArbitrateConfigUtils.class, new Object() {

            @SuppressWarnings("unused")
            @Mock
            public Pipeline getPipeline(Long pipelineId) {
                Pipeline pipeline = new Pipeline();
                pipeline.setId(pipelineId);
                pipeline.setSelectNodes(sourceList);
                pipeline.setExtractNodes(sourceList);
                pipeline.setLoadNodes(targetList);

                return pipeline;
            }

        });

        ExtractRandomLoadBanlance extract = new ExtractRandomLoadBanlance(pipelineId);
        extract.setNodeMonitor(nodeMonitor);
        sleep(500L);
        try {
            Node n1 = extract.next();
            Node n2 = extract.next();
            Node n3 = extract.next();
            Node n4 = extract.next();
            System.out.printf("n1[%s] n2[%s] n3[%s] n4[%s]", n1.getId(), n2.getId(), n3.getId(), n4.getId());
            want.bool(sourceList.contains(n1)).is(true);
            want.bool(sourceList.contains(n2)).is(true);
            want.bool(sourceList.contains(n3)).is(true);
            want.bool(sourceList.contains(n4)).is(true);
        } catch (InterruptedException e) {
            want.fail();
        }

    }

    @Test
    public void testTransform() {
        // 初始化节点
        Mockit.setUpMock(ArbitrateConfigUtils.class, new Object() {

            @SuppressWarnings("unused")
            @Mock
            public Pipeline getPipeline(Long pipelineId) {
                Pipeline pipeline = new Pipeline();
                pipeline.setId(pipelineId);
                pipeline.setSelectNodes(sourceList);
                pipeline.setExtractNodes(sourceList);
                pipeline.setLoadNodes(targetList);
                return pipeline;
            }

        });

        TransformRandomLoadBanlance transform = new TransformRandomLoadBanlance(pipelineId);
        transform.setNodeMonitor(nodeMonitor);
        sleep(500L);
        try {
            Node n1 = transform.next();
            Node n2 = transform.next();
            Node n3 = transform.next();
            Node n4 = transform.next();
            System.out.printf("n1[%s] n2[%s] n3[%s] n4[%s]", n1.getId(), n2.getId(), n3.getId(), n4.getId());
            want.bool(targetList.contains(n1)).is(true);
            want.bool(targetList.contains(n2)).is(true);
            want.bool(targetList.contains(n3)).is(true);
            want.bool(targetList.contains(n4)).is(true);
        } catch (InterruptedException e) {
            want.fail();
        }

    }
}
