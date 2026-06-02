import request from '@/utils/request'

export function getDeviceList(params) {
  return request({
    url: '/device/list',
    method: 'get',
    params
  })
}

export function getDevicePage(params) {
  return request({
    url: '/device/page',
    method: 'get',
    params
  })
}

export function getDeviceById(id) {
  return request({
    url: `/device/${id}`,
    method: 'get'
  })
}

export function createDevice(data) {
  return request({
    url: '/device',
    method: 'post',
    data
  })
}

export function updateDevice(data) {
  return request({
    url: '/device',
    method: 'put',
    data
  })
}

export function deleteDevice(id) {
  return request({
    url: `/device/${id}`,
    method: 'delete'
  })
}

export function updateDeviceStatus(id, status) {
  return request({
    url: `/device/${id}/status`,
    method: 'put',
    params: { status }
  })
}
