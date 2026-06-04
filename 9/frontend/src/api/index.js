import request from '@/utils/request'

export const planApi = {
  page: (params) => request.get('/prod-plan/page', { params }),
  detail: (id) => request.get(`/prod-plan/${id}`),
  create: (data) => request.post('/prod-plan', data),
  audit: (id) => request.put(`/prod-plan/audit/${id}`),
  start: (id) => request.put(`/prod-plan/start/${id}`),
  complete: (data) => request.put('/prod-plan/complete', data),
  cancel: (id) => request.put(`/prod-plan/cancel/${id}`)
}

export const batchApi = {
  page: (params) => request.get('/prod-batch/page', { params }),
  detail: (id) => request.get(`/prod-batch/${id}`),
  available: (params) => request.get('/prod-batch/available', { params }),
  remain: (batchId) => request.get(`/prod-batch/remain/${batchId}`),
  warningStats: (params) => request.get('/prod-batch/warning/stats', { params }),
  warningList: (params) => request.get('/prod-batch/warning/list', { params })
}

export const transferApi = {
  page: (params) => request.get('/stock-transfer/page', { params }),
  detail: (id) => request.get(`/stock-transfer/${id}`),
  create: (data) => request.post('/stock-transfer', data),
  outbound: (id) => request.put(`/stock-transfer/outbound/${id}`),
  inbound: (id) => request.put(`/stock-transfer/inbound/${id}`),
  cancel: (id) => request.put(`/stock-transfer/cancel/${id}`)
}

export const damageApi = {
  page: (params) => request.get('/stock-damage/page', { params }),
  detail: (id) => request.get(`/stock-damage/${id}`),
  create: (data) => request.post('/stock-damage', data),
  audit: (id, params) => request.put(`/stock-damage/audit/${id}`, null, { params })
}

export const analysisApi = {
  getData: (params) => request.get('/analysis', { params }),
  refresh: () => request.post('/analysis/refresh')
}

export const recipeApi = {
  list: (params) => request.get('/recipe/list', { params }),
  detail: (id) => request.get(`/recipe/${id}`)
}

export const materialApi = {
  list: (params) => request.get('/material/list', { params }),
  stock: (params) => request.get('/material/stock', { params })
}

export const storeApi = {
  list: () => request.get('/store/list')
}

export const demandApi = {
  page: (params) => request.get('/store-demand/page', { params }),
  detail: (id) => request.get(`/store-demand/${id}`)
}
