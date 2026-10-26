import request from '@/utils/request'

export function pageSchedules(data) {
  return request({ url: '/clinic/schedule/pageList', method: 'post', data })
}

export function addSchedule(data) {
  return request({ url: '/clinic/schedule/add', method: 'post', data })
}

export function updateSchedule(data) {
  return request({ url: '/clinic/schedule/update', method: 'post', data })
}

export function openSchedule(id) {
  return request({ url: `/clinic/schedule/open/${id}`, method: 'post' })
}

export function closeSchedule(id) {
  return request({ url: `/clinic/schedule/close/${id}`, method: 'post' })
}

export function availableSlots(date, doctorId) {
  return request({
    url: '/clinic/schedule/available',
    method: 'get',
    params: { date, doctorId },
  })
}
