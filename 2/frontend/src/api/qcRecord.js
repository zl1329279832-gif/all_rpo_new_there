import request from '@/utils/request'

export function getQcRecordList(params) {
  return request({
    url: '/qc-records',
    method: 'get',
    params
  })
}

export function getQcRecordPage(params) {
  return request({
    url: '/qc-records',
    method: 'get',
    params
  })
}

export function getQcRecordById(id) {
  return request({
    url: `/qc-records/${id}`,
    method: 'get'
  })
}

export function getQcRecordsByDeviceId(deviceId) {
  return request({
    url: `/qc-records/device/${deviceId}`,
    method: 'get'
  })
}

export function createQcRecord(data) {
  return request({
    url: '/qc-records',
    method: 'post',
    data
  })
}

export function updateQcRecord(data) {
  return request({
    url: '/qc-records',
    method: 'put',
    data
  })
}

export function deleteQcRecord(id) {
  return request({
    url: `/qc-records/${id}`,
    method: 'delete'
  })
}

export function getQcRecordStatistics() {
  return request({
    url: '/qc-records/statistics',
    method: 'get'
  })
}
