package com.hubz.common.util;

import java.util.UUID;

/**
 * @author hubz
 * @date 2021/11/1 22:29
 **/
public final class UuidUtil {

    public static String getUuidAsString32() {
        return uuidToString32(UUID.randomUUID()).replaceAll("-", "");
    }

    public static String uuidToString32(UUID uuid) {
        long leatSigbits = uuid.getLeastSignificantBits();
        long mostSigbits = uuid.getMostSignificantBits();
        return Long.toHexString(mostSigbits) + Long.toHexString(leatSigbits);
    }
}
