package com.passer.littlerpc.core.loadbalance.impl;

import java.util.List;
import java.util.Random;

/**
 * @author passer
 * @time 2021/3/28 10:26 上午
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    public String doSelect(List<String> serviceAddresses) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
