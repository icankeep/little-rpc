package com.passer.littlerpc.core.loadbalance.impl;

import com.passer.littlerpc.core.loadbalance.LoadBalance;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author passer
 * @time 2021/3/28 10:23 上午
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public String selectService(List<String> serviceAddresses) {
        if (CollectionUtils.isEmpty(serviceAddresses)) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses);
    }

    public abstract String doSelect(List<String> serviceAddresses);
}
