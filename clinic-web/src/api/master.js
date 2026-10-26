import request from '@/utils/request'

export function listDepartments() {
  return request({ url: '/clinic/department/listActive', method: 'get' })
}

export function pageDepartments(data) {
  return request({ url: '/clinic/department/pageList', method: 'post', data })
}

export function addDepartment(data) {
  return request({ url: '/clinic/department/add', method: 'post', data })
}

export function updateDepartment(data) {
  return request({ url: '/clinic/department/update', method: 'post', data })
}

export function listDoctors(deptId) {
  return request({
    url: '/clinic/doctor/listActive',
    method: 'get',
    params: deptId ? { deptId } : {},
  })
}

export function pageDoctors(data) {
  return request({ url: '/clinic/doctor/pageList', method: 'post', data })
}

export function addDoctor(data) {
  return request({ url: '/clinic/doctor/add', method: 'post', data })
}

export function updateDoctor(data) {
  return request({ url: '/clinic/doctor/update', method: 'post', data })
}

export function pageDrugs(data) {
  return request({ url: '/clinic/drug/pageList', method: 'post', data })
}

export function addDrug(data) {
  return request({ url: '/clinic/drug/add', method: 'post', data })
}

export function updateDrug(data) {
  return request({ url: '/clinic/drug/update', method: 'post', data })
}

export function listChargeItems() {
  return request({ url: '/clinic/chargeItem/listActive', method: 'get' })
}

export function pageChargeItems(data) {
  return request({ url: '/clinic/chargeItem/pageList', method: 'post', data })
}

export function addChargeItem(data) {
  return request({ url: '/clinic/chargeItem/add', method: 'post', data })
}

export function updateChargeItem(data) {
  return request({ url: '/clinic/chargeItem/update', method: 'post', data })
}
