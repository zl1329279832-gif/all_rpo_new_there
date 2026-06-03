package com.wms.service;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.dto.PickingConfirmDTO;
import com.wms.entity.PickingTask;
import com.wms.entity.PickingTaskDetail;

import java.util.List;

public interface PickingTaskService {

    List<Long> generatePickingTasks(List<Long> shipmentOrderIds, Integer pickingMode,
                                    String picker, Integer priority, String operator);

    void confirmPicking(PickingConfirmDTO dto, String operator);

    void completePicking(Long taskId, String operator);

    PageResult<PickingTask> queryPickingTasks(PageQuery query, Long warehouseId,
                                              Long shipmentOrderId, String picker,
                                              Integer status, Integer priority);

    PickingTask getById(Long id);

    PickingTask getByNo(String taskNo);

    List<PickingTaskDetail> getDetailsByTaskId(Long taskId);
}
