package com.wms.controller;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Location;
import com.wms.service.LocationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "库位管理接口")
@RestController
@RequestMapping("/api/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

    /**
     * 库位列表
     *
     * @param query        分页参数
     * @param warehouseId  仓库ID
     * @param areaId       库区ID
     * @param status       状态
     * @param locationType 库位类型
     * @return 库位列表
     */
    @ApiOperation("库位列表")
    @GetMapping
    public Result<PageResult<Location>> getLocationList(
            @ApiParam("分页参数") @Validated PageQuery query,
            @ApiParam("仓库ID") @RequestParam(required = false) Long warehouseId,
            @ApiParam("库区ID") @RequestParam(required = false) Long areaId,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("库位类型") @RequestParam(required = false) Integer locationType) {
        PageResult<Location> result = locationService.queryLocations(
                query, warehouseId, areaId, status, locationType);
        return Result.success(result);
    }

    /**
     * 库位详情
     *
     * @param id 库位ID
     * @return 库位详情
     */
    @ApiOperation("库位详情")
    @GetMapping("/{id}")
    public Result<Location> getLocationById(@ApiParam("库位ID") @PathVariable Long id) {
        Location location = locationService.getById(id);
        return Result.success(location);
    }

    /**
     * 仓库库位视图数据
     *
     * @param warehouseId 仓库ID
     * @param areaId      库区ID
     * @return 库位视图数据
     */
    @ApiOperation("仓库库位视图数据")
    @GetMapping("/warehouse/{warehouseId}")
    public Result<List<Location>> getLocationView(
            @ApiParam("仓库ID") @PathVariable Long warehouseId,
            @ApiParam("库区ID") @RequestParam(required = false) Long areaId) {
        List<Location> list = locationService.getLocationView(warehouseId, areaId);
        return Result.success(list);
    }

    /**
     * 新增库位
     *
     * @param location 库位信息
     * @param operator 操作人
     * @return 库位ID
     */
    @ApiOperation("新增库位")
    @PostMapping
    public Result<Long> createLocation(
            @ApiParam("库位信息") @Validated @RequestBody Location location,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        Long id = locationService.createLocation(location, operator != null ? operator : "system");
        return Result.success(id);
    }

    /**
     * 更新库位
     *
     * @param location 库位信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("更新库位")
    @PutMapping
    public Result<Void> updateLocation(
            @ApiParam("库位信息") @Validated @RequestBody Location location,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        locationService.updateLocation(location, operator != null ? operator : "system");
        return Result.success();
    }
}
