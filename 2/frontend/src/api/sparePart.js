import request from '@/utils/request'

export function getSparePartList(params) {
  return request({
    url: '/spare-parts',
    method: 'get',
    params
  })
}

export function getSparePartPage(params) {
  return request({
    url: '/spare-parts',
    method: 'get',
    params
  })
}

export function getSparePartById(id) {
  return request({
    url: `/spare-parts/${id}`,
    method: 'get'
  })
}

export function createSparePart(data) {
  return request({
    url: '/spare-parts',
    method: 'post',
    data
  })
}

export function updateSparePart(data) {
  return request({
    url: '/spare-parts',
    method: 'put',
    data
  })
}

export function deleteSparePart(id) {
  return request({
    url: `/spare-parts/${id}`,
    method: 'delete'
  })
}

export function stockInSparePart(id, quantity) {
  return request({
    url: `/spare-parts/${id}/stock-in`,
    method: 'put',
    params: { quantity }
  })
}

export function stockOutSparePart(id, quantity) {
  return request({
    url: `/spare-parts/${id}/stock-out`,
    method: 'put',
    params: { quantity }
  })
}

export function getLowStockSpareParts() {
  return request({
    url: '/spare-parts/low-stock',
    method: 'get'
  })
}
