package com.wms.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusinessType {

    INBOUND(1, "入库", 1),
    OUTBOUND(2, "出库", 2),
    TRANSFER(3, "调拨", 0),
    STOCKTAKE(4, "盘点", 0),
    FREEZE(5, "冻结", 0),
    UNFREEZE(6, "解冻", 0),
    RETURN(7, "退货", 1),
    ADJUST(8, "调整", 0);

    private final Integer code;
    private final String name;
    private final Integer operationType;
}
