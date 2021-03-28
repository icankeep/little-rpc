package com.passer.littlerpc.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author passer
 * @time 2021/3/27 6:10 下午
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {
    GZIP((byte) 0, "gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (CompressTypeEnum c : CompressTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
