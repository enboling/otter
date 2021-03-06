package com.alibaba.otter.shared.common.utils.thread;

import org.springframework.aop.TargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * 获取池化的{@linkplain ExecutorTemplate}
 * 
 * @author jianghang 2013-3-1 下午08:05:43
 * @version 4.1.7
 */
public class ExecutorTemplateGetter implements BeanFactoryAware {

    private BeanFactory beanFactory;

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public ExecutorTemplate get() {
        TargetSource targetSource = (TargetSource) this.beanFactory.getBean("executorTemplateTargetSource");
        try {
            return (ExecutorTemplate) targetSource.getTarget();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void release(ExecutorTemplate target) {
        TargetSource targetSource = (TargetSource) this.beanFactory.getBean("executorTemplateTargetSource");
        try {
            targetSource.releaseTarget(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
