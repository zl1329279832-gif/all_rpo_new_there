package com.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel("盘点结果录入DTO")
public class StocktakeResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "盘点单ID", required = true, example = "1")
    @NotNull(message = "盘点单ID不能为空")
    private Long stocktakeId;

    @ApiModelProperty(value = "盘点人", example = "admin")
    private String counter;

    @ApiModelProperty(value = "盘点明细", required = true)
    @NotEmpty(message = "盘点明细不能为空")
    @Valid
    private List<StocktakeDetailDTO> details;

    @Data
    @ApiModel("盘点明细DTO")
    public static class StocktakeDetailDTO implements Serializable {
        @ApiModelProperty(value = "盘点明细ID", required = true, example = "1")
        @NotNull(message = "盘点明细ID不能为空")
        private Long detailId;

        @ApiModelProperty(value = "初盘数量", required = true, example = "100")
        @NotNull(message = "初盘数量不能为空")
        private BigDecimal firstCount;

        @ApiModelProperty(value = "复盘数量", example = "100")
        private BigDecimal secondCount;

        @ApiModelProperty(value = "最终数量", required = true, example = "100")
        @NotNull(message = "最终数量不能为空")
        private BigDecimal finalCount;

        @ApiModelProperty(value = "差异原因")
        private String diffReason;
    }
}
