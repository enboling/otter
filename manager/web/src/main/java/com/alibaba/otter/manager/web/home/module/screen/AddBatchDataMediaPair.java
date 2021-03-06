package com.alibaba.otter.manager.web.home.module.screen;

import javax.annotation.Resource;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.web.common.WebConstant;

public class AddBatchDataMediaPair {

    @Resource(name = "channelService")
    private ChannelService channelService;

    public void execute(@Param("pipelineId") Long pipelineId, Context context, Navigator nav) throws Exception {

        Channel channel = channelService.findByPipelineId(pipelineId);
        if (channel.getStatus().isStart()) {
            nav.redirectTo(WebConstant.ERROR_FORBIDDEN_Link);
            return;
        }

        context.put("pipelineId", pipelineId);
        context.put("channelId", channel.getId());
    }
}
