<template>
  <div class="order-create-container">
    <van-nav-bar
      title="创建订单"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <!-- 服务信息卡片 -->
      <div class="service-card" v-if="currentService.id">
        <div class="service-header">
          <div class="service-icon" :style="{ background: currentService.gradient }">
            <van-icon :name="currentService.icon" size="32" />
          </div>
          <div class="service-details">
            <h3>{{ currentService.name }}</h3>
            <p>{{ currentService.description }}</p>
            <div class="service-price">¥{{ currentService.price }}/{{ currentService.unit || '次' }}</div>
          </div>
        </div>
        
        <!-- 服务数量选择 -->
        <div class="quantity-section">
          <span class="quantity-label">服务数量</span>
          <van-stepper 
            v-model="serviceQuantity" 
            :min="1"
            :max="10"
            integer
          />
        </div>
      </div>

      <!-- 预约信息 -->
      <van-form v-if="currentService.id" @submit="onSubmit">
        <!-- 选择日期 -->
        <div class="form-section">
          <div class="section-title">选择服务日期</div>
          <van-calendar
            v-model:show="showCalendar"
            :min-date="minDate"
            :max-date="maxDate"
            @confirm="onDateConfirm"
          />
          <van-field
            v-model="formData.date"
            label="服务日期"
            placeholder="请选择服务日期"
            readonly
            is-link
            @click="showCalendar = true"
            :rules="[{ required: true, message: '请选择服务日期' }]"
          />
        </div>

        <!-- 选择时间 -->
        <div class="form-section">
          <div class="section-title">选择服务时间</div>
          <div class="time-slots">
            <div
              v-for="slot in timeSlots"
              :key="slot.value"
              class="time-slot"
              :class="{ 
                active: formData.time === slot.value,
                disabled: slot.disabled
              }"
              @click="selectTime(slot)"
            >
              <div class="slot-time">{{ slot.label }}</div>
              <div class="slot-status" v-if="slot.disabled">已约满</div>
            </div>
          </div>
        </div>

        <!-- 服务地址 -->
        <div class="form-section">
          <div class="section-title">
            <span>服务地址</span>
          </div>
          
          <!-- 美化的选择地址按钮 -->
          <div class="select-address-btn" @click="showAddressPicker = true">
            <div class="btn-icon">
              <van-icon name="location-o" size="24" />
            </div>
            <div class="btn-text">点击选择地址</div>
          </div>
          
          <van-field
            v-model="formData.address"
            label="详细地址"
            placeholder="请输入详细地址"
            :rules="[{ required: true, message: '请输入服务地址' }]"
          />
          <van-field
            v-model="formData.contactName"
            label="联系人"
            placeholder="请输入联系人姓名"
            :rules="[{ required: true, message: '请输入联系人姓名' }]"
          />
          <van-field
            v-model="formData.contactPhone"
            label="联系电话"
            placeholder="请输入联系电话"
            :rules="[
              { required: true, message: '请输入联系电话' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
            ]"
          />
        </div>

        <!-- 服务需求 -->
        <div class="form-section">
          <div class="section-title">服务需求说明</div>
          <van-field
            v-model="formData.requirements"
            type="textarea"
            placeholder="请详细描述您的服务需求，以便我们为您提供更好的服务"
            rows="3"
            autosize
            show-word-limit
            maxlength="200"
          />
        </div>

        <!-- 特殊要求 -->
        <div class="form-section">
          <div class="section-title">特殊要求（可选）</div>
          <van-checkbox-group v-model="formData.specialRequests">
            <van-checkbox name="tools">自带工具</van-checkbox>
            <van-checkbox name="materials">自带材料</van-checkbox>
            <van-checkbox name="urgent">加急服务（+¥30）</van-checkbox>
            <van-checkbox name="insurance">需要保险（+¥20）</van-checkbox>
          </van-checkbox-group>
        </div>

        <!-- 费用预估 -->
        <div class="cost-section">
          <div class="section-title">费用预估</div>
          <div class="cost-card">
            <div class="cost-item">
              <span>{{ currentService.name }} x {{ serviceQuantity }}</span>
              <span>¥{{ (parseFloat(currentService.price) * serviceQuantity).toFixed(2) }}</span>
            </div>
            <div class="cost-item" v-if="urgentFee > 0">
              <span>加急费用</span>
              <span>¥{{ urgentFee }}</span>
            </div>
            <div class="cost-item" v-if="insuranceFee > 0">
              <span>保险费用</span>
              <span>¥{{ insuranceFee }}</span>
            </div>
            <div class="cost-divider"></div>
            <div class="cost-total">
              <span>预估总费用</span>
              <span class="total-amount">¥{{ totalCost }}</span>
            </div>
            <div class="cost-note">
              *实际费用以服务完成后结算为准
            </div>
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="submit-section">
          <van-button
            block
            type="primary"
            native-type="submit"
            size="large"
            :loading="submitting"
            :disabled="!currentService.id"
          >
            确认预约（¥{{ totalCost }}）
          </van-button>
        </div>
      </van-form>
    </div>

    <!-- 地址选择弹窗 -->
    <van-popup 
      v-model:show="showAddressPicker" 
      position="bottom" 
      :style="{ height: '60%' }"
      round
    >
      <div class="address-picker">
        <div class="picker-header">
          <h3>选择地址</h3>
          <van-icon name="cross" @click="showAddressPicker = false" />
        </div>
        <div class="picker-content">
          <van-loading v-if="loadingAddress" class="loading-center" />
          <van-empty 
            v-else-if="addressList.length === 0"
            description="暂无地址" 
          >
            <van-button 
              type="primary" 
              size="small"
              @click="goToAddressBook"
            >
              去添加
            </van-button>
          </van-empty>
          <div v-else class="address-list">
            <div 
              v-for="address in addressList" 
              :key="address.id"
              class="address-item"
              @click="selectAddress(address)"
            >
              <div class="address-info">
                <div class="address-header">
                  <span class="address-name">{{ address.name }}</span>
                  <span class="address-phone">{{ address.phone }}</span>
                  <van-tag v-if="address.isDefault === 1" type="danger" size="small">默认</van-tag>
                </div>
                <div class="address-detail">
                  {{ formatAddress(address) }}
                </div>
                <div class="address-label" v-if="address.label">
                  <van-tag size="small">{{ address.label }}</van-tag>
                </div>
              </div>
              <van-icon name="arrow" />
            </div>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 预约成功弹窗 -->
    <van-popup
      v-model:show="showSuccessPopup"
      round
      :close-on-click-overlay="false"
    >
      <div class="success-popup">
        <div class="success-icon">
          <van-icon name="success" color="#07c160" size="48" />
        </div>
        <h3>预约成功！</h3>
        <div class="success-info">
          <p>预约单号：{{ appointmentId }}</p>
          <p>服务时间：{{ formData.date }} {{ getTimeLabel(formData.time) }}</p>
          <p>服务内容：{{ currentService.name }} x {{ serviceQuantity }}</p>
          <p>我们会在24小时内与您联系确认服务详情</p>
        </div>
        <div class="success-actions">
          <van-button
            block
            type="primary"
            @click="goToAppointmentList"
          >
            查看我的预约
          </van-button>
          <van-button
            block
            type="default"
            @click="goBack"
            style="margin-top: 12px;"
          >
            返回首页
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 底部导航栏 -->
    <BottomTabbar />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showDialog } from 'vant'
import BottomTabbar from '@/components/BottomTabbar.vue'
import { createAppointment } from '@/api/appointment'
import appointmentApi from '@/api/appointment'
import { getAddressBookList, type AddressBook } from '@/api/addressBook'

const router = useRouter()
const route = useRoute()

// 响应式数据
const showCalendar = ref(false)
const showAddressPicker = ref(false)
const submitting = ref(false)
const showSuccessPopup = ref(false)
const appointmentId = ref('')
const loadingAddress = ref(false)

// 当前服务
const currentService = ref<any>({})

// 服务数量
const serviceQuantity = ref(1)

// 地址列表
const addressList = ref<AddressBook[]>([])

// 表单数据
const formData = ref({
  date: '',
  time: '',
  address: '',
  contactName: '',
  contactPhone: '',
  requirements: '',
  specialRequests: [] as string[]
})

// 日期范围
const minDate = new Date()
const maxDate = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) // 30天后

// 时间段
const timeSlots = ref([
  { label: '08:00-10:00', value: '08:00', disabled: false },
  { label: '10:00-12:00', value: '10:00', disabled: false },
  { label: '14:00-16:00', value: '14:00', disabled: false },
  { label: '16:00-18:00', value: '16:00', disabled: false },
  { label: '18:00-20:00', value: '18:00', disabled: false }
])

// 根据服务类型获取样式配置
const getServiceStyleConfig = (serviceType: string) => {
  const styleConfigs: Record<string, any> = {
    'cleaning': {
      icon: 'brush-o',
      gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    },
    'repair': {
      icon: 'setting-o',
      gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
    },
    'appliance': {
      icon: 'tv-o',
      gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
    },
    'moving': {
      icon: 'logistics',
      gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
    },
    'tutoring': {
      icon: 'friends-o',
      gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
    },
    'gardening': {
      icon: 'flower-o',
      gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
    },
    'pest': {
      icon: 'delete-o',
      gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
    }
  }
  
  return styleConfigs[serviceType] || {
    icon: 'service',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  }
}

// 计算属性 - 服务总费用
const servicesTotalCost = computed(() => {
  return parseFloat(currentService.value.price || 0) * serviceQuantity.value
})

// 计算属性 - 加急费用
const urgentFee = computed(() => {
  return formData.value.specialRequests.includes('urgent') ? 30 : 0
})

// 计算属性 - 保险费用
const insuranceFee = computed(() => {
  return formData.value.specialRequests.includes('insurance') ? 20 : 0
})

// 计算属性 - 总费用
const totalCost = computed(() => {
  return (servicesTotalCost.value + urgentFee.value + insuranceFee.value).toFixed(2)
})

// 页面初始化
onMounted(async () => {
  // 加载地址列表并自动填充默认地址
  await loadAddressList()
  
  // 如果路由带有服务ID参数，加载该服务信息
  const serviceId = route.query.serviceId as string
  console.log('接收到的 serviceId:', serviceId, '类型:', typeof serviceId)
  
  if (serviceId && serviceId !== 'undefined') {
    const serviceIdNum = parseInt(serviceId)
    console.log('转换后的 serviceId:', serviceIdNum)
    
    if (!isNaN(serviceIdNum)) {
      await loadServiceInfo(serviceIdNum)
    } else {
      showToast('服务ID格式不正确')
      router.back()
    }
  } else {
    showToast('请先选择服务')
    router.back()
  }
})

// 加载服务信息
const loadServiceInfo = async (serviceId: number) => {
  try {
    // 获取所有服务列表
    const res = await appointmentApi.getServiceTypes()
    console.log('API返回的服务列表:', res)
    
    if (res.code === 200 && res.data) {
      console.log('服务数据:', res.data)
      console.log('查找的服务ID:', serviceId, '类型:', typeof serviceId)
      
      // 查找指定的服务 - 使用 == 进行宽松比较，或者确保类型一致
      const service = res.data.find((s: any) => {
        console.log('比较服务:', s.id, '(类型:', typeof s.id, ') === ', serviceId, '(类型:', typeof serviceId, ') ?', s.id == serviceId)
        return Number(s.id) === Number(serviceId) // 确保两边都是数字类型
      })
      
      if (service) {
        console.log('找到服务:', service)
        // 添加样式配置
        const styleConfig = getServiceStyleConfig(service.type)
        currentService.value = {
          ...service,
          ...styleConfig
        }
      } else {
        console.error('服务不存在，服务ID:', serviceId)
        showToast('服务不存在')
        router.back()
      }
    }
  } catch (error) {
    console.error('加载服务信息失败:', error)
    showToast('加载服务信息失败')
  }
}

// 加载地址列表
const loadAddressList = async () => {
  try {
    loadingAddress.value = true
    const res = await getAddressBookList()
    
    if (res.code === 200 && res.data) {
      addressList.value = res.data
      
      // 自动填充默认地址
      const defaultAddress = res.data.find(addr => addr.isDefault === 1)
      if (defaultAddress) {
        selectAddress(defaultAddress, false) // false 表示不关闭弹窗
      }
    }
  } catch (error) {
    console.error('加载地址列表失败:', error)
  } finally {
    loadingAddress.value = false
  }
}

// 选择地址
const selectAddress = (address: AddressBook, closePopup = true) => {
  // 格式化完整地址
  const fullAddress = `${address.province}${address.city}${address.district}${address.detail}`
  
  formData.value.address = fullAddress
  formData.value.contactName = address.name
  formData.value.contactPhone = address.phone
  
  if (closePopup) {
    showAddressPicker.value = false
    showToast('已选择地址')
  }
}

// 格式化地址显示
const formatAddress = (address: AddressBook) => {
  return `${address.province}${address.city}${address.district}${address.detail}`
}

// 跳转到地址簿管理
const goToAddressBook = () => {
  showAddressPicker.value = false
  router.push('/address-book')
}

// 返回上一页
const onClickLeft = () => {
  router.back()
}

// 日期确认
const onDateConfirm = (date: Date) => {
  formData.value.date = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
  showCalendar.value = false
}

// 选择时间
const selectTime = (slot: any) => {
  if (slot.disabled) {
    showToast('该时间段已约满')
    return
  }
  formData.value.time = slot.value
}

// 获取时间标签
const getTimeLabel = (time: string) => {
  const slot = timeSlots.value.find(s => s.value === time)
  return slot?.label || time
}

// 提交表单
const onSubmit = async () => {
  // 检查是否选择了服务
  if (!currentService.value.id) {
    showToast('请选择服务')
    return
  }
  
  // 检查是否选择了时间
  if (!formData.value.time) {
    showToast('请选择服务时间')
    return
  }
  
  submitting.value = true
  
  try {
    // 拼接日期和时间，使用 ISO 8601 格式（带 T）
    const appointmentDateTime = `${formData.value.date}T${formData.value.time}:00`
    
    // 拼接需求说明和特殊要求
    let requirements = formData.value.requirements || ''
    if (formData.value.specialRequests && formData.value.specialRequests.length > 0) {
      if (requirements) requirements += ' | '
      requirements += '特殊要求：'
      const requestTexts = formData.value.specialRequests.map(req => {
        switch (req) {
          case 'tools': return '自带工具'
          case 'materials': return '自带材料'
          case 'urgent': return '加急服务'
          case 'insurance': return '需要保险'
          default: return req
        }
      })
      requirements += requestTexts.join('、')
    }
    
    // 添加数量信息到需求说明
    if (serviceQuantity.value > 1) {
      requirements = `数量：${serviceQuantity.value}份 ${requirements ? '| ' + requirements : ''}`
    }
    
    // 计算预估价格：基础价格 * 数量 + 特殊要求附加费
    let estimatedPrice = parseFloat(currentService.value.price) * serviceQuantity.value
    
    // 处理特殊要求费用
    if (formData.value.specialRequests && formData.value.specialRequests.length > 0) {
      formData.value.specialRequests.forEach(req => {
        if (req === 'urgent') {
          estimatedPrice += 30 // 加急服务加30元
        } else if (req === 'insurance') {
          estimatedPrice += 20 // 保险加20元
        }
      })
    }
    
    console.log('提交的数据:', {
      serviceType: currentService.value.type,
      appointmentTime: appointmentDateTime,
      address: formData.value.address,
      contactName: formData.value.contactName,
      contactPhone: formData.value.contactPhone,
      requirements: requirements,
      estimatedPrice: estimatedPrice
    })
    
    // 调用真实的API
    const res = await createAppointment({
      serviceType: currentService.value.type,
      appointmentTime: appointmentDateTime,
      address: formData.value.address,
      contactName: formData.value.contactName,
      contactPhone: formData.value.contactPhone,
      requirements: requirements,
      estimatedPrice: estimatedPrice
    })
    
    if (res.code === 200) {
      // 设置订单编号
      appointmentId.value = res.data
      showSuccessPopup.value = true
    } else {
      showToast(res.message || '预约失败，请重试')
    }
  } catch (error: any) {
    console.error('创建订单失败:', error)
    // 判断是否是余额不足错误
    const errorMsg = error.message || error.msg || '预约失败，请重试'
    
    if (errorMsg.includes('余额不足') || errorMsg.includes('账户余额不足')) {
      showDialog({
        title: '余额不足',
        message: `当前订单需要支付 ¥${estimatedPrice.toFixed(2)}，您的账户余额不足。请先充值后再继续预约。`,
        confirmButtonText: '前往充值',
        cancelButtonText: '取消'
      }).then(() => {
        // 跳转到充值页面（如果有的话）
        // router.push('/user/recharge')
        showToast('充值功能开发中')
      }).catch(() => {
        // 用户点击取消
      })
    } else {
      showToast(errorMsg)
    }
  } finally {
    submitting.value = false
  }
}

// 跳转到预约列表
const goToAppointmentList = () => {
  showSuccessPopup.value = false
  router.replace('/appointment/list')
}

// 返回首页
const goBack = () => {
  showSuccessPopup.value = false
  router.replace('/appointment')
}
</script>

<style scoped lang="scss">
.order-create-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-top: 46px;
  padding-bottom: 60px;
}

.content {
  padding: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 12px;
  
  .van-icon {
    color: #1989fa;
  }
}

// 已选服务列表
.service-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  .service-header {
    display: flex;
    gap: 16px;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
    
    .service-icon {
      width: 64px;
      height: 64px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      flex-shrink: 0;
    }
    
    .service-details {
      flex: 1;
      
      h3 {
        margin: 0 0 6px 0;
        font-size: 18px;
        font-weight: 600;
        color: #323233;
      }
      
      p {
        margin: 0 0 8px 0;
        font-size: 14px;
        color: #969799;
        line-height: 1.4;
      }
      
      .service-price {
        font-size: 16px;
        color: #ee0a24;
        font-weight: 600;
      }
    }
  }
  
  .quantity-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .quantity-label {
      font-size: 15px;
      font-weight: 500;
      color: #323233;
    }
  }
}

// 表单区域
.form-section,
.cost-section {
  margin-bottom: 16px;
}

// 美化的选择地址按钮
.select-address-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);
  position: relative;
  overflow: hidden;
  
  // 悬浮动画效果
  &:active {
    transform: translateY(2px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  }
  
  // 背景闪光效果
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
    transform: scale(0);
    transition: transform 0.6s ease;
  }
  
  &:active::before {
    transform: scale(1);
  }
  
  .btn-icon {
    width: 56px;
    height: 56px;
    background: rgba(255, 255, 255, 0.25);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    backdrop-filter: blur(10px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  .btn-text {
    font-size: 18px;
    font-weight: 600;
    color: white;
    letter-spacing: 1px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
}

.time-slots {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  
  .time-slot {
    background-color: #fff;
    border: 1px solid #ebedf0;
    border-radius: 8px;
    padding: 16px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;
    
    &.active {
      border-color: #1989fa;
      background-color: #e8f4ff;
      color: #1989fa;
    }
    
    &.disabled {
      background-color: #f7f8fa;
      color: #c8c9cc;
      cursor: not-allowed;
    }
    
    .slot-time {
      font-size: 14px;
      font-weight: 500;
      margin-bottom: 4px;
    }
    
    .slot-status {
      font-size: 12px;
    }
  }
}

.cost-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
  
  .cost-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    font-size: 14px;
  }
  
  .cost-divider {
    height: 1px;
    background-color: #ebedf0;
    margin: 8px 0;
  }
  
  .cost-total {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    font-size: 16px;
    font-weight: 600;
    
    .total-amount {
      color: #ee0a24;
      font-size: 20px;
    }
  }
  
  .cost-note {
    font-size: 12px;
    color: #969799;
    margin-top: 8px;
    text-align: center;
  }
}

.submit-section {
  margin-top: 24px;
}

// 地址选择弹窗
.address-picker {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .picker-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #ebedf0;
    
    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
    }
    
    .van-icon {
      font-size: 20px;
      cursor: pointer;
    }
  }
  
  .picker-content {
    flex: 1;
    overflow-y: auto;
    
    .loading-center {
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 60px 0;
    }
    
    .address-list {
      padding: 12px;
      
      .address-item {
        background: #fff;
        border-radius: 12px;
        padding: 16px;
        margin-bottom: 12px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 12px;
        cursor: pointer;
        transition: all 0.3s;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        
        &:active {
          transform: scale(0.98);
          background-color: #f7f8fa;
        }
        
        .address-info {
          flex: 1;
          
          .address-header {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 8px;
            
            .address-name {
              font-size: 16px;
              font-weight: 600;
              color: #323233;
            }
            
            .address-phone {
              font-size: 14px;
              color: #646566;
            }
          }
          
          .address-detail {
            font-size: 14px;
            color: #969799;
            line-height: 1.5;
            margin-bottom: 6px;
          }
          
          .address-label {
            margin-top: 6px;
          }
        }
        
        .van-icon {
          color: #c8c9cc;
          font-size: 16px;
        }
      }
    }
  }
}

// 成功弹窗样式
.success-popup {
  padding: 32px 24px 24px;
  text-align: center;
  
  .success-icon {
    margin-bottom: 16px;
  }
  
  h3 {
    margin: 0 0 16px 0;
    font-size: 18px;
    font-weight: 600;
  }
  
  .success-info {
    margin-bottom: 24px;
    
    p {
      margin: 0 0 8px 0;
      font-size: 14px;
      color: #646566;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

:deep(.van-field__label) {
  width: 80px;
}

:deep(.van-checkbox) {
  margin-bottom: 12px;
}

:deep(.van-cell-group) {
  border-radius: 12px;
  overflow: hidden;
}
</style>

