package com.passer.littlerpc.common.remoting.dto;

import com.passer.littlerpc.common.entity.RpcServiceProperty;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = -8048550434730381489L;

    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] params;
    private Class<?>[] paramTypes;
    private String version;
    private String group;

    public RpcServiceProperty toRpcServiceProperty() {
        RpcServiceProperty property = RpcServiceProperty.builder()
                .serviceName(this.getInterfaceName())
                .group(this.getGroup())
                .version(this.getVersion())
                .build();
        return property;
    }
}
