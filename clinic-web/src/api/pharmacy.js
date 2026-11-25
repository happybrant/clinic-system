import request from '@/utils/request'

export function pageDispenses(data) {
  return request({ url: '/clinic/pharmacy/pageList', method: 'post', data })
}

export function getDispenseDetail(id) {
  return request({ url: `/clinic/pharmacy/getDetail/${id}`, method: 'get' })
}

export function approveDispense(data) {
  return request({ url: '/clinic/pharmacy/approve', method: 'post', data })
}

export function rejectDispense(data) {
  return request({ url: '/clinic/pharmacy/reject', method: 'post', data })
}

export function doDispense(data) {
  return request({ url: '/clinic/pharmacy/dispense', method: 'post', data })
}
