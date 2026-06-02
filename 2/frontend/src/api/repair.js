import request from '@/utils/request'

export function getRepairOrderList(params) {
  return request({
    url: '/repair-order/list',
    method: 'get',
    params
  })
}

export function getRepairOrderPage(params) {
  return request({
    url: '/repair-order/page',
    method: 'get',
    params
  })
}

export function getRepairOrderById(id) {
  return request({
    url: `/repair-order/${id}`,
    method: 'get'
  })
}

export function createRepairOrder(data) {
  return request({
    url: '/repair-order',
    method: 'post',
    data
  })
}

export function updateRepairOrder(data) {
  return request({
    url: '/repair-order',
    method: 'put',
    data
  })
}

export function deleteRepairOrder(id) {
  return request({
    url: `/repair-order/${id}`,
    method: 'delete'
  })
}

export function updateRepairOrderStatus(id, status) {
  return request({
    url: `/repair-order/${id}/status`,
    method: 'put',
    params: { status }
  })
}
