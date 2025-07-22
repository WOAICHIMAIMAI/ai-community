<template>
  <div class="booking-container">
    <van-nav-bar
      :title="serviceInfo.name"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <!-- 服务信息卡片 -->
      <div class="service-card">
        <div class="service-header">
          <div class="service-icon" :class="serviceType">
            <van-icon :name="serviceInfo.icon" size="32" />
          </div>
          <div class="service-details">
            <h3>{{ serviceInfo.name }}</h3>
            <p>{{ serviceInfo.description }}</p>
            <div class="service-price">¥{{ serviceInfo.price }}/次起</div>
          </div>
        </div>
      </div>

      <!-- 预约表单 -->
      <van-form @submit="onSubmit">
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
          <div class="section-title">服务地址</div>
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
            <van-checkbox name="urgent">加急服务</van-checkbox>
            <van-checkbox name="insurance">需要保险</van-checkbox>
          </van-checkbox-group>
        </div>

        <!-- 费用预估 -->
        <div class="cost-section">
          <div class="section-title">费用预估</div>
          <div class="cost-card">
            <div class="cost-item">
              <span>基础服务费</span>
              <span>¥{{ basePrice }}</span>
            </div>
            <div class="cost-item" v-if="urgentFee > 0">
              <span>加急费用</span>
              <span>¥{{ urgentFee }}</span>
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
          >
            确认预约
          </van-button>
        </div>
      </van-form>
    </div>

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
import { showToast, showSuccessToast } from 'vant'
import BottomTabbar from '@/components/BottomTabbar.vue'

const router = useRouter()
const route = useRoute()

// 响应式数据
const serviceType = ref('')
const showCalendar = ref(false)
const submitting = ref(false)
const showSuccessPopup = ref(false)
const appointmentId = ref('')

// 表单数据
const formData = ref({
  date: '',
  time: '',
  address: '',
  contactName: '',
  contactPhone: '',
  requirements: '',
  specialRequests: []
})

// 日期范围
const minDate = new Date()
const maxDate = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) // 30天后

// 时间段
const timeSlots = ref([
  { label: '08:00-10:00', value: '08:00', disabled: false },
  { label: '10:00-12:00', value: '10:00', disabled: false },
  { label: '14:00-16:00', value: '14:00', disabled: true },
  { label: '16:00-18:00', value: '16:00', disabled: false },
  { label: '18:00-20:00', value: '18:00', disabled: false }
])

// 服务信息
const serviceInfo = ref({
  name: '',
  icon: '',
  description: '',
  price: '0'
})

// 服务类型配置
const serviceConfig: Record<string, any> = {
  cleaning: {
    name: '家政保洁',
    icon: 'brush-o',
    description: '专业保洁团队，深度清洁您的家',
    price: '80'
  },
  repair: {
    name: '维修服务',
    icon: 'setting-o',
    description: '专业维修师傅，解决各种维修问题',
    price: '50'
  },
  moving: {
    name: '搬家服务',
    icon: 'logistics',
    description: '专业搬家团队，安全快捷',
    price: '200'
  },
  appliance: {
    name: '家电维修',
    icon: 'tv-o',
    description: '家电故障维修，快速上门',
    price: '60'
  },
  gardening: {
    name: '园艺服务',
    icon: 'flower-o',
    description: '花草养护，园艺设计',
    price: '100'
  },
  pest: {
    name: '除虫服务',
    icon: 'delete-o',
    description: '专业除虫，安全环保',
    price: '120'
  }
}

// 计算属性
const basePrice = computed(() => {
  return parseFloat(serviceInfo.value.price)
})

const urgentFee = computed(() => {
  return formData.value.specialRequests.includes('urgent') ? 30 : 0
})

const totalCost = computed(() => {
  return basePrice.value + urgentFee.value
})

// 页面初始化
onMounted(() => {
  serviceType.value = route.params.serviceType as string
  
  if (serviceConfig[serviceType.value]) {
    serviceInfo.value = serviceConfig[serviceType.value]
  } else {
    showToast('服务类型不存在')
    router.back()
  }
})

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
  if (!formData.value.time) {
    showToast('请选择服务时间')
    return
  }
  
  submitting.value = true
  
  try {
    // 模拟提交过程
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 生成预约单号
    appointmentId.value = 'APT' + Date.now()
    
    showSuccessPopup.value = true
  } catch (error) {
    showToast('预约失败，请重试')
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
.booking-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-top: 46px;
}

.content {
  padding: 16px;
}

.service-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  
  .service-header {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .service-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &.cleaning {
        background-color: #e8f5e8;
        color: #4fc08d;
      }
      
      &.repair {
        background-color: #e6f7ff;
        color: #1890ff;
      }
      
      &.moving {
        background-color: #fff7e6;
        color: #fa8c16;
      }
      
      &.appliance {
        background-color: #f6ffed;
        color: #52c41a;
      }
      
      &.gardening {
        background-color: #f0f9ff;
        color: #0ea5e9;
      }
      
      &.pest {
        background-color: #fee2e2;
        color: #ef4444;
      }
    }
    
    .service-details {
      flex: 1;
      
      h3 {
        margin: 0 0 4px 0;
        font-size: 18px;
        font-weight: 600;
      }
      
      p {
        margin: 0 0 8px 0;
        font-size: 14px;
        color: #646566;
      }
      
      .service-price {
        font-size: 16px;
        color: #ff6b35;
        font-weight: 600;
      }
    }
  }
}

.form-section,
.cost-section {
  margin-bottom: 20px;
  
  .section-title {
    font-size: 16px;
    font-weight: 500;
    color: #323233;
    margin-bottom: 12px;
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
      border-color: #4fc08d;
      background-color: #e8f5e8;
      color: #4fc08d;
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
      color: #ff6b35;
      font-size: 18px;
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
  
  .success-actions {
    .van-button {
      &:not(:last-child) {
        margin-bottom: 12px;
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
