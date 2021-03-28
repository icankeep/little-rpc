package com.passer.littlerpc.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author passer
 * @time 2021/3/27 4:05 下午
 */
@AllArgsConstructor
@Getter
public enum SerializerTypeEnum {
    KYRO((byte) 0, "kyro"), PROTOSTUFF((byte) 1, "protostuff");
    private byte code;
    private String name;

    public static String getName(byte code) {
        for (SerializerTypeEnum serializerTypeEnum : SerializerTypeEnum.values()) {
            if (serializerTypeEnum.code == code) {
                return serializerTypeEnum.getName();
            }
        }
        return null;
    }
}
