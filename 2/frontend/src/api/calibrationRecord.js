import request from '@/utils/request'

export function getCalibrationRecordList(params) {
  return request({
    url: '/calibration-records',
    method: 'get',
    params
  })
}

export function getCalibrationRecordPage(params) {
  return request({
    url: '/calibration-records',
    method: 'get',
    params
  })
}

export function getCalibrationRecordById(id) {
  return request({
    url: `/calibration-records/${id}`,
    method: 'get'
  })
}

export function getCalibrationRecordsByDeviceId(deviceId) {
  return request({
    url: `/calibration-records/device/${deviceId}`,
    method: 'get'
  })
}

export function createCalibrationRecord(data) {
  return request({
    url: '/calibration-records',
    method: 'post',
    data
  })
}

export function updateCalibrationRecord(data) {
  return request({
    url: '/calibration-records',
    method: 'put',
    data
  })
}

export function deleteCalibrationRecord(id) {
  return request({
    url: `/calibration-records/${id}`,
    method: 'delete'
  })
}
