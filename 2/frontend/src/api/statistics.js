import request from '@/utils/request'

export function getOverallStatistics() {
  return request({
    url: '/statistics/overall',
    method: 'get'
  })
}

export function getDeviceStats() {
  return request({
    url: '/devices/statistics',
    method: 'get'
  })
}

export function getRepairStats() {
  return request({
    url: '/repair-orders/statistics',
    method: 'get'
  })
}

export function getInspectionStats() {
  return request({
    url: '/inspection/statistics',
    method: 'get'
  })
}
