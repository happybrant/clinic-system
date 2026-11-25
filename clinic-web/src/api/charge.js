import request from '@/utils/request'

export function createChargeFromPrescription(prescriptionId) {
  return request({
    url: '/clinic/charge/createFromPrescription',
    method: 'post',
    data: { prescriptionId },
  })
}

export function payCharge(data) {
  return request({ url: '/clinic/charge/pay', method: 'post', data })
}

export function cancelCharge(id) {
  return request({ url: `/clinic/charge/cancel/${id}`, method: 'post' })
}

export function pageCharges(data) {
  return request({ url: '/clinic/charge/pageList', method: 'post', data })
}

export function getChargeDetail(id) {
  return request({ url: `/clinic/charge/getDetail/${id}`, method: 'get' })
}
