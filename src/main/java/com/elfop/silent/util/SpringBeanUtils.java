package com.elfop.silent.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2019/12/9  10:17
 */
public class SpringBeanUtils implements BeanFactoryAware {
    private static BeanFactory beanFactory = null;
    private static SpringBeanUtils beanUtils = null;

    public static BeanFactory getBeanFactory() {
        return beanFactory;
    }

    @Override
    public void setBeanFactory(BeanFactory source) throws BeansException {
        beanFactory = source;
    }

    public static SpringBeanUtils getInstance() {
        if (beanUtils == null) {
            beanUtils = (SpringBeanUtils) beanFactory.getBean("beanUtils");
        }
        return beanUtils;
    }

    public static Object getBean(String beanName) {
        return beanFactory.getBean(beanName);
    }

    public static Object getBean(String beanName, Class clazz) {
        return beanFactory.getBean(beanName, clazz);
    }

}
