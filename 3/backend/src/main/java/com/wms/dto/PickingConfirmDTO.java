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
@ApiModel("拣货确认DTO")
public class PickingConfirmDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "拣货任务ID", required = true, example = "1")
    @NotNull(message = "拣货任务ID不能为空")
    private Long taskId;

    @ApiModelProperty(value = "拣货员", example = "picker")
    private String picker;

    @ApiModelProperty(value = "拣货明细", required = true)
    @NotEmpty(message = "拣货明细不能为空")
    @Valid
    private List<PickingDetailDTO> details;

    @Data
    @ApiModel("拣货明细DTO")
    public static class PickingDetailDTO implements Serializable {
        @ApiModelProperty(value = "拣货任务明细ID", required = true, example = "1")
        @NotNull(message = "拣货任务明细ID不能为空")
        private Long taskDetailId;

        @ApiModelProperty(value = "实际拣货数量", required = true, example = "10")
        @NotNull(message = "实际拣货数量不能为空")
        private BigDecimal pickedQuantity;
    }
}
