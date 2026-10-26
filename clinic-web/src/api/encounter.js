import request from '@/utils/request'

export function walkIn(data) {
  return request({ url: '/clinic/encounter/walkIn', method: 'post', data })
}

export function pageEncounters(data) {
  return request({ url: '/clinic/encounter/pageList', method: 'post', data })
}

export function callNext(data) {
  return request({ url: '/clinic/encounter/callNext', method: 'post', data })
}

export function startEncounter(id) {
  return request({ url: `/clinic/encounter/start/${id}`, method: 'post' })
}

export function skipEncounter(id) {
  return request({ url: `/clinic/encounter/skip/${id}`, method: 'post' })
}

export function finishEncounter(id) {
  return request({ url: `/clinic/encounter/finish/${id}`, method: 'post' })
}
