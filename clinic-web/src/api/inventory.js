import request from '@/utils/request'

export function pageStock(data) {
  return request({ url: '/clinic/inventory/stock/pageList', method: 'post', data })
}

export function expireWarn() {
  return request({ url: '/clinic/inventory/stock/expireWarn', method: 'get' })
}

export function saveInbound(data) {
  return request({ url: '/clinic/inventory/inbound/save', method: 'post', data })
}

export function postInbound(id) {
  return request({ url: `/clinic/inventory/inbound/post/${id}`, method: 'post' })
}

export function pageInbounds(data) {
  return request({ url: '/clinic/inventory/inbound/pageList', method: 'post', data })
}

export function getInboundDetail(id) {
  return request({ url: `/clinic/inventory/inbound/getDetail/${id}`, method: 'get' })
}

export function pageBounds(data) {
  return request({ url: '/clinic/inventory/bound/pageList', method: 'post', data })
}

export function getBoundDetail(id) {
  return request({ url: `/clinic/inventory/bound/getDetail/${id}`, method: 'get' })
}

export function saveStocktake(data) {
  return request({ url: '/clinic/inventory/stocktake/save', method: 'post', data })
}

export function postStocktake(id) {
  return request({ url: `/clinic/inventory/stocktake/post/${id}`, method: 'post' })
}

export function pageStocktakes(data) {
  return request({ url: '/clinic/inventory/stocktake/pageList', method: 'post', data })
}

export function getStocktakeDetail(id) {
  return request({ url: `/clinic/inventory/stocktake/getDetail/${id}`, method: 'get' })
}
