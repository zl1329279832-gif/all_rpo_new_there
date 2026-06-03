import request from '@/utils/request'

export function getMaintenanceContractList(params) {
  return request({
    url: '/maintenance-contracts',
    method: 'get',
    params
  })
}

export function getMaintenanceContractPage(params) {
  return request({
    url: '/maintenance-contracts',
    method: 'get',
    params
  })
}

export function getMaintenanceContractById(id) {
  return request({
    url: `/maintenance-contracts/${id}`,
    method: 'get'
  })
}

export function createMaintenanceContract(data) {
  return request({
    url: '/maintenance-contracts',
    method: 'post',
    data
  })
}

export function updateMaintenanceContract(data) {
  return request({
    url: '/maintenance-contracts',
    method: 'put',
    data
  })
}

export function deleteMaintenanceContract(id) {
  return request({
    url: `/maintenance-contracts/${id}`,
    method: 'delete'
  })
}

export function updateMaintenanceContractStatus(id, status) {
  return request({
    url: `/maintenance-contracts/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function getExpiringContracts(days) {
  return request({
    url: '/maintenance-contracts/expiring',
    method: 'get',
    params: { days }
  })
}
