package com.wms.dto;

import com.wms.common.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("库存查询DTO")
public class InventoryQueryDTO extends PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "仓库ID", example = "1")
    private Long warehouseId;

    @ApiModelProperty(value = "库位ID", example = "1")
    private Long locationId;

    @ApiModelProperty(value = "库区ID", example = "1")
    private Long areaId;

    @ApiModelProperty(value = "商品ID", example = "1")
    private Long productId;

    @ApiModelProperty(value = "商品编码")
    private String productCode;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @ApiModelProperty(value = "供应商ID", example = "1")
    private Long supplierId;

    @ApiModelProperty(value = "库存状态:1-正常 2-临期 3-过期 4-冻结")
    private List<Integer> inventoryStatus;

    @ApiModelProperty(value = "是否只查询有库存")
    private Boolean onlyHasStock;

    @ApiModelProperty(value = "生产日期开始")
    private Date produceDateStart;

    @ApiModelProperty(value = "生产日期结束")
    private Date produceDateEnd;

    @ApiModelProperty(value = "过期日期开始")
    private Date expireDateStart;

    @ApiModelProperty(value = "过期日期结束")
    private Date expireDateEnd;

    @ApiModelProperty(value = "入库日期开始")
    private Date inboundDateStart;

    @ApiModelProperty(value = "入库日期结束")
    private Date inboundDateEnd;
}
