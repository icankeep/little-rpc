package com.passer.littlerpc.core.loadbalance;

import com.passer.littlerpc.common.annotation.SPI;

import java.util.List;

/**
 * @author passer
 * @time 2021/3/28 10:21 上午
 */
@SPI
public interface LoadBalance {
    String selectService(List<String> serviceAddresses);
}
