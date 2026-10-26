import request from '@/utils/request'

export function bookAppointment(data) {
  return request({ url: '/clinic/appointment/book', method: 'post', data })
}

export function cancelAppointment(data) {
  return request({ url: '/clinic/appointment/cancel', method: 'post', data })
}

export function checkInAppointment(data) {
  return request({ url: '/clinic/appointment/checkIn', method: 'post', data })
}

export function pageAppointments(data) {
  return request({ url: '/clinic/appointment/pageList', method: 'post', data })
}

export function getAppointmentDetail(id) {
  return request({ url: `/clinic/appointment/getDetail/${id}`, method: 'get' })
}
