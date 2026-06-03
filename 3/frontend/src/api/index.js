import request from '@/utils/request'

export const loginApi = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export const logoutApi = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

export const getBatchListApi = (params) => {
  return request({
    url: '/inventory/batch',
    method: 'get',
    params
  })
}

export const getBatchByIdApi = (id) => {
  return request({
    url: `/inventory/batch/${id}`,
    method: 'get'
  })
}

export const traceBatchApi = (batchNo, params) => {
  return request({
    url: `/inventory/batch/trace/${batchNo}`,
    method: 'get',
    params
  })
}

export const freezeInventoryApi = (data) => {
  return request({
    url: '/inventory/freeze',
    method: 'post',
    data
  })
}

export const unfreezeInventoryApi = (data) => {
  return request({
    url: '/inventory/unfreeze',
    method: 'post',
    data
  })
}

export const getInventoryLogListApi = (params) => {
  return request({
    url: '/inventory/log',
    method: 'get',
    params
  })
}

export const getReceiptListApi = (params) => {
  return request({
    url: '/receipt',
    method: 'get',
    params
  })
}

export const getReceiptDetailApi = (id) => {
  return request({
    url: `/receipt/${id}`,
    method: 'get'
  })
}

export const createReceiptOrderApi = (data) => {
  return request({
    url: '/receipt',
    method: 'post',
    data
  })
}

export const confirmArrivalApi = (id, data) => {
  return request({
    url: `/receipt/${id}/arrival`,
    method: 'post',
    data
  })
}

export const doInspectionApi = (id, data) => {
  return request({
    url: `/receipt/${id}/inspect`,
    method: 'post',
    data
  })
}

export const assignLocationApi = (id, data) => {
  return request({
    url: `/receipt/${id}/putaway`,
    method: 'post',
    data
  })
}

export const confirmReceiptCompleteApi = (id) => {
  return request({
    url: `/receipt/${id}/confirm`,
    method: 'post'
  })
}

export const getShipmentListApi = (params) => {
  return request({
    url: '/shipment',
    method: 'get',
    params
  })
}

export const getShipmentDetailApi = (id) => {
  return request({
    url: `/shipment/${id}`,
    method: 'get'
  })
}

export const createShipmentOrderApi = (data) => {
  return request({
    url: '/shipment',
    method: 'post',
    data
  })
}

export const allocateInventoryApi = (id) => {
  return request({
    url: `/shipment/${id}/allocate`,
    method: 'post'
  })
}

export const cancelShipmentApi = (id, data) => {
  return request({
    url: `/shipment/${id}/cancel`,
    method: 'post',
    data
  })
}

export const confirmShipmentCompleteApi = (id) => {
  return request({
    url: `/shipment/${id}/confirm`,
    method: 'post'
  })
}

export const getPickingListApi = (params) => {
  return request({
    url: '/picking',
    method: 'get',
    params
  })
}

export const getPickingDetailApi = (id) => {
  return request({
    url: `/picking/${id}`,
    method: 'get'
  })
}

export const generatePickingTasksApi = (data) => {
  return request({
    url: '/picking/generate',
    method: 'post',
    data
  })
}

export const confirmPickingApi = (id, data) => {
  return request({
    url: `/picking/${id}/confirm`,
    method: 'post',
    data
  })
}

export const completePickingApi = (id) => {
  return request({
    url: `/picking/${id}/complete`,
    method: 'post'
  })
}

export const getStocktakeListApi = (params) => {
  return request({
    url: '/stocktake',
    method: 'get',
    params
  })
}

export const getStocktakeDetailApi = (id) => {
  return request({
    url: `/stocktake/${id}`,
    method: 'get'
  })
}

export const createStocktakeOrderApi = (data) => {
  return request({
    url: '/stocktake',
    method: 'post',
    data
  })
}

export const enterStocktakeResultApi = (id, data) => {
  return request({
    url: `/stocktake/${id}/result`,
    method: 'post',
    data
  })
}

export const confirmStocktakeCompleteApi = (id, data) => {
  return request({
    url: `/stocktake/${id}/confirm`,
    method: 'post',
    data
  })
}

export const getReturnListApi = (params) => {
  return request({
    url: '/return',
    method: 'get',
    params
  })
}

export const getReturnDetailApi = (id) => {
  return request({
    url: `/return/${id}`,
    method: 'get'
  })
}

export const createReturnOrderApi = (data) => {
  return request({
    url: '/return',
    method: 'post',
    data
  })
}

export const doReturnInspectionApi = (id, data) => {
  return request({
    url: `/return/${id}/inspect`,
    method: 'post',
    data
  })
}

export const confirmReturnCompleteApi = (id, data) => {
  return request({
    url: `/return/${id}/confirm`,
    method: 'post',
    data
  })
}

export const getAlertListApi = (params) => {
  return request({
    url: '/alert',
    method: 'get',
    params
  })
}

export const checkAlertApi = () => {
  return request({
    url: '/alert/check',
    method: 'post'
  })
}

export const handleAlertApi = (id, data) => {
  return request({
    url: `/alert/${id}/handle`,
    method: 'post',
    data
  })
}

export const getAlertDashboardApi = () => {
  return request({
    url: '/alert/dashboard',
    method: 'get'
  })
}

export const getLocationListApi = (params) => {
  return request({
    url: '/location',
    method: 'get',
    params
  })
}

export const getLocationByIdApi = (id) => {
  return request({
    url: `/location/${id}`,
    method: 'get'
  })
}

export const getLocationViewApi = (warehouseId, params) => {
  return request({
    url: `/location/warehouse/${warehouseId}`,
    method: 'get',
    params
  })
}

export const createLocationApi = (data) => {
  return request({
    url: '/location',
    method: 'post',
    data
  })
}

export const updateLocationApi = (data) => {
  return request({
    url: '/location',
    method: 'put',
    data
  })
}

export const getProductListApi = (params) => {
  return request({
    url: '/product',
    method: 'get',
    params
  })
}

export const getProductByIdApi = (id) => {
  return request({
    url: `/product/${id}`,
    method: 'get'
  })
}

export const createProductApi = (data) => {
  return request({
    url: '/product',
    method: 'post',
    data
  })
}

export const updateProductApi = (data) => {
  return request({
    url: '/product',
    method: 'put',
    data
  })
}

export const getSupplierListApi = (params) => {
  return request({
    url: '/supplier',
    method: 'get',
    params
  })
}

export const getSupplierByIdApi = (id) => {
  return request({
    url: `/supplier/${id}`,
    method: 'get'
  })
}

export const createSupplierApi = (data) => {
  return request({
    url: '/supplier',
    method: 'post',
    data
  })
}

export const updateSupplierApi = (data) => {
  return request({
    url: '/supplier',
    method: 'put',
    data
  })
}

export const getWarehouseListApi = (params) => {
  return request({
    url: '/warehouse',
    method: 'get',
    params
  })
}

export const getWarehouseByIdApi = (id) => {
  return request({
    url: `/warehouse/${id}`,
    method: 'get'
  })
}

export const getAllWarehousesApi = () => {
  return request({
    url: '/warehouse/all',
    method: 'get'
  })
}

export const getReportOverviewApi = () => {
  return request({
    url: '/report/overview',
    method: 'get'
  })
}

export const getReportTrendApi = (params) => {
  return request({
    url: '/report/trend',
    method: 'get',
    params
  })
}

export const getReportByWarehouseApi = () => {
  return request({
    url: '/report/warehouse',
    method: 'get'
  })
}

export const exportReportApi = (params) => {
  return request({
    url: '/report/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
