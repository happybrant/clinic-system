import request from '@/utils/request'

export function savePrescription(data) {
  return request({ url: '/clinic/prescription/save', method: 'post', data })
}

export function submitPrescription(id) {
  return request({ url: `/clinic/prescription/submit/${id}`, method: 'post' })
}

export function voidPrescription(id) {
  return request({ url: `/clinic/prescription/void/${id}`, method: 'post' })
}

export function pagePrescriptions(data) {
  return request({ url: '/clinic/prescription/pageList', method: 'post', data })
}

export function getPrescriptionDetail(id) {
  return request({ url: `/clinic/prescription/getDetail/${id}`, method: 'get' })
}
