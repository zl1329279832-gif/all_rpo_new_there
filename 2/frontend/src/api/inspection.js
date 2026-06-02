import request from '@/utils/request'

export function getInspectionPlans(params) {
  return request({
    url: '/inspection/plans',
    method: 'get',
    params
  })
}

export function createInspectionPlan(data) {
  return request({
    url: '/inspection/plans',
    method: 'post',
    data
  })
}

export function updateInspectionPlan(id, data) {
  return request({
    url: `/inspection/plans/${id}`,
    method: 'put',
    data
  })
}

export function getInspectionTasks(params) {
  return request({
    url: '/inspection/tasks',
    method: 'get',
    params
  })
}

export function getCalendarTasks(startDate, endDate) {
  return request({
    url: '/inspection/tasks/calendar',
    method: 'get',
    params: { startDate, endDate }
  })
}

export function getTaskDetail(id) {
  return request({
    url: `/inspection/tasks/${id}`,
    method: 'get'
  })
}

export function executeTask(id, checkResult, abnormalDesc, handleSuggestion) {
  return request({
    url: `/inspection/tasks/${id}/execute`,
    method: 'put',
    params: { checkResult, abnormalDesc, handleSuggestion }
  })
}

export function getInspectionStatistics() {
  return request({
    url: '/inspection/statistics',
    method: 'get'
  })
}
