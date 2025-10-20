import { get, post, put, del } from '@/utils/request'

// 地址簿接口
export interface AddressBook {
  id: number
  userId: number
  name: string        // 收货人姓名
  phone: string       // 联系电话
  province: string    // 省份
  city: string        // 城市
  district: string    // 区县
  detail: string      // 详细地址
  postalCode?: string // 邮政编码
  isDefault: number   // 是否默认(0-否 1-是)
  label?: string      // 地址标签(家/公司/学校等)
  createTime?: string
  updateTime?: string
}

// 地址簿列表响应
export interface AddressBookListResponse {
  code: number
  data: AddressBook[]
  message: string
}

// 地址簿详情响应
export interface AddressBookDetailResponse {
  code: number
  data: AddressBook
  message: string
}

// 通用响应
export interface CommonResponse {
  code: number
  message: string
}

/**
 * 获取地址簿列表
 * @returns 地址簿列表
 */
export function getAddressBookList(): Promise<AddressBookListResponse> {
  return get<AddressBookListResponse>('/api/user/addressBook/list')
}

/**
 * 获取地址簿详情
 * @param id 地址ID
 * @returns 地址详情
 */
export function getAddressBookDetail(id: number): Promise<AddressBookDetailResponse> {
  return get<AddressBookDetailResponse>(`/api/user/addressBook/${id}`)
}

/**
 * 新增地址
 * @param address 地址信息
 * @returns 新增结果
 */
export function addAddressBook(address: Partial<AddressBook>): Promise<CommonResponse> {
  return post<CommonResponse>('/api/user/addressBook/add', address)
}

/**
 * 更新地址
 * @param id 地址ID
 * @param address 地址信息
 * @returns 更新结果
 */
export function updateAddressBook(id: number, address: Partial<AddressBook>): Promise<CommonResponse> {
  return put<CommonResponse>(`/api/user/addressBook/${id}`, address)
}

/**
 * 删除地址
 * @param id 地址ID
 * @returns 删除结果
 */
export function deleteAddressBook(id: number): Promise<CommonResponse> {
  return del<CommonResponse>(`/api/user/addressBook/${id}`)
}

/**
 * 设置默认地址
 * @param id 地址ID
 * @returns 设置结果
 */
export function setDefaultAddress(id: number): Promise<CommonResponse> {
  return put<CommonResponse>(`/api/user/addressBook/${id}/default`)
}

