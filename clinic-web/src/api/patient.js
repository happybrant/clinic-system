import request from '@/utils/request'

export function pagePatients(data) {
  return request({
    url: '/clinic/patient/pageList',
    method: 'post',
    data,
  })
}

export function getPatientDetail(id) {
  return request({
    url: `/clinic/patient/getDetail/${id}`,
    method: 'get',
  })
}

export function addPatient(data) {
  return request({
    url: '/clinic/patient/add',
    method: 'post',
    data,
  })
}

export function updatePatient(data) {
  return request({
    url: '/clinic/patient/update',
    method: 'post',
    data,
  })
}
