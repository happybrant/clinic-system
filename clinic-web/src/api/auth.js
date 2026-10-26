import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/security/user/login',
    method: 'post',
    data: {
      account: data.account,
      password: data.password,
    },
  })
}

export function logout() {
  return request({
    url: '/security/user/logout',
    method: 'post',
  })
}
