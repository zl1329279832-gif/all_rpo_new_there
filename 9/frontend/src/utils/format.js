import dayjs from 'dayjs'

export const planStatusMap = {
  0: { label: '待审核', class: 'status-0' },
  1: { label: '已审核', class: 'status-1' },
  2: { label: '生产中', class: 'status-2' },
  3: { label: '已完成', class: 'status-3' },
  4: { label: '已取消', class: 'status-4' }
}

export const demandStatusMap = {
  0: { label: '待确认', class: 'status-0' },
  1: { label: '已确认', class: 'status-1' },
  2: { label: '已发货', class: 'status-2' },
  3: { label: '已完成', class: 'status-3' }
}

export const transferStatusMap = {
  0: { label: '待出库', class: 'status-0' },
  1: { label: '已出库', class: 'status-1' },
  2: { label: '已入库', class: 'status-3' },
  3: { label: '已取消', class: 'status-4' }
}

export const transferTypeMap = {
  1: { label: '正常调拨', class: 'status-1' },
  2: { label: '临期调拨', class: 'status-2' }
}

export const damageStatusMap = {
  0: { label: '待审核', class: 'status-0' },
  1: { label: '已审核', class: 'status-3' },
  2: { label: '已驳回', class: 'status-4' }
}

export const damageTypeMap = {
  1: '临期过期',
  2: '质量问题',
  3: '破损',
  4: '其他'
}

export const batchStatusMap = {
  1: { label: '在库', class: 'status-1' },
  2: { label: '部分出库', class: 'status-2' },
  3: { label: '已售罄', class: 'status-3' },
  4: { label: '已报损', class: 'status-4' }
}

export const formatDateTime = (val) => {
  if (!val) return '-'
  return dayjs(val).format('YYYY-MM-DD HH:mm:ss')
}

export const formatDate = (val) => {
  if (!val) return '-'
  return dayjs(val).format('YYYY-MM-DD')
}

export const formatMoney = (val) => {
  if (val === null || val === undefined) return '-'
  return '¥' + Number(val).toFixed(2)
}

export const formatNumber = (val) => {
  if (val === null || val === undefined) return '-'
  return Number(val).toFixed(2)
}

export const getWarningStatus = (expireTime, warningHours = 12) => {
  if (!expireTime) return 'normal'
  const now = dayjs()
  const expire = dayjs(expireTime)
  if (expire.isBefore(now)) {
    return 'expired'
  }
  const diffHours = expire.diff(now, 'hour')
  if (diffHours <= warningHours) {
    return 'expiring'
  }
  return 'normal'
}

export const getWarningLabel = (status) => {
  const map = {
    expired: '已过期',
    expiring: '临期',
    normal: '正常'
  }
  return map[status] || '正常'
}
