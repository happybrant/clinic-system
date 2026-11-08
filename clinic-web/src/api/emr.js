import request from '@/utils/request'

export function saveEmr(data) {
  return request({ url: '/clinic/emr/save', method: 'post', data })
}

export function submitEmr(id) {
  return request({ url: `/clinic/emr/submit/${id}`, method: 'post' })
}

export function getEmrByEncounter(encounterId) {
  return request({ url: `/clinic/emr/getByEncounter/${encounterId}`, method: 'get' })
}

export function historyEmrByPatient(patientId) {
  return request({ url: `/clinic/emr/historyByPatient/${patientId}`, method: 'get' })
}
