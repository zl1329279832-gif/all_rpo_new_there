import request from '@/utils/request'

export function getDeviceList(params) {
  return request({
    url: '/devices',
    method: 'get',
    params
  })
}

export function getDevicePage(params) {
  return request({
    url: '/devices',
    method: 'get',
    params
  })
}

export function getDeviceById(id) {
  return request({
    url: `/devices/${id}`,
    method: 'get'
  })
}

export function createDevice(data) {
  return request({
    url: '/devices',
    method: 'post',
    data
  })
}

export function updateDevice(id, data) {
  return request({
    url: `/devices/${id}`,
    method: 'put',
    data
  })
}

export function deleteDevice(id) {
  return request({
    url: `/devices/${id}`,
    method: 'delete'
  })
}

export function updateDeviceQcStatus(id, qcStatus) {
  return request({
    url: `/devices/${id}/qc-status`,
    method: 'put',
    params: { qcStatus }
  })
}

export function getHighRiskDevices() {
  return request({
    url: '/devices/high-risk',
    method: 'get'
  })
}

export function getDeviceStatistics() {
  return request({
    url: '/devices/statistics',
    method: 'get'
  })
}
