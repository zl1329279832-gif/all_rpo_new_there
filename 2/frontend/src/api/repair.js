import request from '@/utils/request'

export function getRepairOrderList(params) {
  return request({
    url: '/repair-orders',
    method: 'get',
    params
  })
}

export function getRepairOrderPage(params) {
  return request({
    url: '/repair-orders',
    method: 'get',
    params
  })
}

export function getRepairOrderById(id) {
  return request({
    url: `/repair-orders/${id}`,
    method: 'get'
  })
}

export function createRepairOrder(data) {
  return request({
    url: '/repair-orders',
    method: 'post',
    data
  })
}

export function assignOrder(id, repairerId, repairerName) {
  return request({
    url: `/repair-orders/${id}/assign`,
    method: 'put',
    params: { repairerId, repairerName }
  })
}

export function startRepair(id) {
  return request({
    url: `/repair-orders/${id}/start`,
    method: 'put'
  })
}

export function completeRepair(id, repairContent, repairResult, parts) {
  return request({
    url: `/repair-orders/${id}/complete`,
    method: 'put',
    params: { repairContent, repairResult },
    data: parts
  })
}

export function acceptOrder(id, qcStatus) {
  return request({
    url: `/repair-orders/${id}/accept`,
    method: 'put',
    params: { qcStatus }
  })
}

export function getRepairStatistics() {
  return request({
    url: '/repair-orders/statistics',
    method: 'get'
  })
}
