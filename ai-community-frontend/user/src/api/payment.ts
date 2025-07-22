import request from '@/utils/request'

// 用户账户信息接口
export interface UserAccount {
  id: number
  userId: number
  balance: number
  frozenAmount: number
  availableBalance: number
  totalRecharge: number
  totalConsumption: number
  status: number
  statusName: string
}

// 账单信息接口
export interface PaymentBill {
  id: number
  billType: number
  billTypeName: string
  billTitle: string
  billDescription: string
  amount: number
  billingPeriod: string
  dueDate: string
  status: number
  statusName: string
  isOverdue: boolean
  createdTime: string
}

// 缴费记录接口
export interface PaymentRecord {
  id: number
  billId: number
  billTitle: string
  billType: number
  billTypeName: string
  paymentNo: string
  paymentAmount: number
  paymentMethod: number
  paymentMethodName: string
  transactionId: string
  paymentStatus: number
  paymentStatusName: string
  paymentTime: string
  billingPeriod: string
  remark: string
  createdTime: string
}

// 缴费统计信息接口
export interface PaymentStatistics {
  totalAmount: number
  totalCount: number
  avgAmount: number
  monthAmount: number
  yearAmount: number
  pendingAmount: number
  pendingCount: number
}

// 账单查询参数
export interface BillQueryParams {
  billType?: number
  status?: number
  billingPeriod?: string
  year?: number
  month?: number
  page?: number
  pageSize?: number
}

// 创建缴费订单参数
export interface PaymentCreateParams {
  billIds: number[]
  paymentMethod: number
  paymentAmount: number
  remark?: string
}

// 账户充值参数
export interface RechargeParams {
  amount: number
  paymentMethod: number
  remark?: string
}

// 分页响应接口
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 获取用户账户信息
 */
export const getUserAccount = () => {
  return request.get<UserAccount>('/user/payment/account')
}

/**
 * 账户充值
 */
export const rechargeAccount = (params: RechargeParams) => {
  return request.post<boolean>('/user/payment/account/recharge', params)
}

/**
 * 获取待缴费账单列表
 */
export const getPendingBills = () => {
  return request.get<PaymentBill[]>('/user/payment/bills/pending')
}

/**
 * 获取待缴费总金额
 */
export const getPendingAmount = () => {
  return request.get<number>('/user/payment/bills/pending/amount')
}

/**
 * 分页查询账单列表
 */
export const getBillPage = (params: BillQueryParams) => {
  return request.get<PageResponse<PaymentBill>>('/user/payment/bills', { params })
}

/**
 * 获取账单详情
 */
export const getBillDetail = (billId: number) => {
  return request.get<PaymentBill>(`/user/payment/bills/${billId}`)
}

/**
 * 创建缴费订单
 */
export const createPaymentOrder = (params: PaymentCreateParams) => {
  return request.post<PaymentRecord>('/user/payment/orders', params)
}

/**
 * 确认支付
 */
export const confirmPayment = (recordId: number, transactionId: string) => {
  return request.post<boolean>(`/user/payment/orders/${recordId}/confirm`, null, {
    params: { transactionId }
  })
}

/**
 * 分页查询缴费记录
 */
export const getPaymentRecordPage = (params: BillQueryParams) => {
  return request.get<PageResponse<PaymentRecord>>('/user/payment/records', { params })
}

/**
 * 获取缴费统计信息
 */
export const getPaymentStatistics = (year?: number) => {
  return request.get<PaymentStatistics>('/user/payment/statistics', {
    params: { year }
  })
}

/**
 * 根据账单ID查询缴费记录
 */
export const getPaymentRecordByBillId = (billId: number) => {
  return request.get<PaymentRecord>(`/user/payment/records/bill/${billId}`)
}

// 支付方式接口
export interface PaymentMethod {
  id: number
  name: string
  description: string
  icon: string
  type: number
  enabled: boolean
  balance?: number
  cardNumber?: string
  bankName?: string
  isDefault: boolean
  sort: number
}

/**
 * 获取支付方式列表
 */
export const getPaymentMethods = () => {
  return request.get<PaymentMethod[]>('/user/payment/methods')
}
