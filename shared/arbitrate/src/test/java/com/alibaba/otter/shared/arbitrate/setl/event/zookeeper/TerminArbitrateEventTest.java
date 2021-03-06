package com.alibaba.otter.shared.arbitrate.setl.event.zookeeper;

import java.util.ArrayList;
import java.util.List;

import mockit.Mock;
import mockit.Mockit;

import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.helper.StagePathUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.ExtractZooKeeperArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.LoadZooKeeperArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.SelectZooKeeperArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.TerminZooKeeperArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.TransformZooKeeperArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData.TerminType;
import com.alibaba.otter.shared.arbitrate.setl.event.BaseArbitrateEventTest;

/**
 * @author jianghang 2011-9-27 下午09:45:23
 * @version 4.0.0
 */
public class TerminArbitrateEventTest extends BaseArbitrateEventTest {

    private SelectZooKeeperArbitrateEvent    selectEvent;
    private ExtractZooKeeperArbitrateEvent   extractEvent;
    private TransformZooKeeperArbitrateEvent transformEvent;
    private LoadZooKeeperArbitrateEvent      loadEvent;
    private TerminZooKeeperArbitrateEvent    terminEvent;

    @Test
    public void test_Rollback() {
        normalProcess();
        // 发送rollback信号
        TerminEventData rollback = new TerminEventData();
        rollback.setPipelineId(pipelineId);
        rollback.setType(TerminType.ROLLBACK);
        terminEvent.single(rollback);

        PermitMonitor monitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        want.bool(monitor.getChannelPermit().isPause()).is(true);

        destoryTermin();
        ArbitrateFactory.destory(pipelineId);
    }

    @Test
    public void test_Shutdown() {
        normalProcess();
        // 发送shutdown信号
        TerminEventData shutdown = new TerminEventData();
        shutdown.setPipelineId(pipelineId);
        shutdown.setType(TerminType.SHUTDOWN);
        terminEvent.single(shutdown);

        PermitMonitor monitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        want.bool(monitor.getChannelPermit().isStop()).is(true);

        destoryTermin();
        ArbitrateFactory.destory(pipelineId);
    }

    @Test
    public void test_Restart() {
        normalProcess();
        // 发送restart信号
        TerminEventData rollback = new TerminEventData();
        rollback.setPipelineId(pipelineId);
        rollback.setType(TerminType.RESTART);
        terminEvent.single(rollback);

        PermitMonitor monitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        sleep(4000L);
        want.bool(monitor.getChannelPermit().isStart()).is(true);

        // 发送shutdown信号
        TerminEventData shutdown = new TerminEventData();
        shutdown.setPipelineId(pipelineId);
        shutdown.setType(TerminType.SHUTDOWN);
        terminEvent.single(shutdown);

        want.bool(monitor.getChannelPermit().isStop()).is(true);

        // 删除对应的错误节点
        destoryTermin();
        ArbitrateFactory.destory(pipelineId);
    }

    private void destoryTermin() {
        String path = StagePathUtils.getTerminRoot(pipelineId);
        List<String> terminNodes = zookeeper.getChildren(path);
        for (String node : terminNodes) {
            // 删除对应的错误节点
            TerminEventData termin = new TerminEventData();
            termin.setPipelineId(pipelineId);
            termin.setProcessId(StagePathUtils.getProcessId(node));
            System.out.println("remove termin node: " + path + "/" + node);
            terminEvent.ack(termin);// 发送ack信号，删除termin节点
        }
    }

    private void normalProcess() {
        Mockit.setUpMock(ArbitrateConfigUtils.class, new Object() {

            @SuppressWarnings("unused")
            @Mock
            public int getParallelism(Long pipelineId) {
                return 2;// 并行度
            }

            @SuppressWarnings("unused")
            @Mock
            public Long getCurrentNid() {
                return nid;
            }

        });

        selectEvent = new SelectZooKeeperArbitrateEvent();
        extractEvent = new ExtractZooKeeperArbitrateEvent();
        transformEvent = new TransformZooKeeperArbitrateEvent();
        loadEvent = new LoadZooKeeperArbitrateEvent();
        terminEvent = (TerminZooKeeperArbitrateEvent) this.getBeanFactory().getBean("terminZooKeeperEvent");
        loadEvent.setTerminEvent(terminEvent);

        final List<Long> initProcessIds = new ArrayList<Long>();
        try {
            // 获取数据

            // select stage
            EtlEventData sdata1 = selectEvent.await(pipelineId);
            EtlEventData sdata2 = selectEvent.await(pipelineId);

            initProcessIds.add(sdata1.getProcessId());
            initProcessIds.add(sdata2.getProcessId());

            selectEvent.single(sdata1);
            selectEvent.single(sdata2);

            // extract stage
            EtlEventData edata1 = extractEvent.await(pipelineId);
            EtlEventData edata2 = extractEvent.await(pipelineId);

            extractEvent.single(edata1);
            extractEvent.single(edata2);

            // transform stage
            EtlEventData tdata1 = transformEvent.await(pipelineId);
            EtlEventData tdata2 = transformEvent.await(pipelineId);

            transformEvent.single(tdata1);
            transformEvent.single(tdata2);

            // SelectStageListener selectStageListener =
            // ArbitrateFactory.getInstance(pipelineId,
            // SelectStageListener.class);
            // selectStageListener.destory();
            // load stage
            EtlEventData ldata1 = loadEvent.await(pipelineId);
            loadEvent.single(ldata1);
            Long p1 = ldata1.getProcessId();

            TerminEventData terminData1 = new TerminEventData();
            terminData1.setPipelineId(pipelineId);
            terminData1.setProcessId(p1);
            terminEvent.ack(terminData1);// 发送ack信号，删除termin节点

        } catch (InterruptedException e) {
            want.fail();
        }

    }
}
