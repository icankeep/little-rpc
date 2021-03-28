package com.passer.littlerpc;

import com.passer.littlerpc.common.extension.ExtensionLoader;
import com.passer.littlerpc.core.registry.ServiceRegistry;
import com.passer.littlerpc.core.registry.zk.ZkServiceRegistry;
import org.junit.Assert;
import org.junit.Test;



/**
 * @author passer
 * @time 2021/3/28 3:39 下午
 */
public class ExtensionLoaderTest {

    @Test
    public void loadExtension() {
        ServiceRegistry serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
        Assert.assertTrue(serviceRegistry.getClass().equals(ZkServiceRegistry.class));
    }
}
