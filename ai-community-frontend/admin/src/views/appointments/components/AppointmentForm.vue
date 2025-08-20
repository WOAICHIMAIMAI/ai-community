<template>
  <div class="appointment-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="用户" prop="userId">
            <el-select
              v-model="form.userId"
              placeholder="选择用户"
              filterable
              remote
              :remote-method="searchUsers"
              :loading="userLoading"
              style="width: 100%"
            >
              <el-option
                v-for="user in userOptions"
                :key="user.id"
                :label="`${user.username} (${user.phone})`"
                :value="user.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        
        <el-col :span="12">
          <el-form-item label="服务类型" prop="serviceId">
            <el-select
              v-model="form.serviceId"
              placeholder="选择服务"
              style="width: 100%"
              @change="handleServiceChange"
            >
              <el-option
                v-for="service in serviceOptions"
                :key="service.id"
                :label="`${service.serviceName} (¥${service.price})`"
                :value="service.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="预约时间" prop="appointmentTime">
            <el-date-picker
              v-model="form.appointmentTime"
              type="datetime"
              placeholder="选择预约时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
              :disabled-date="disabledDate"
              :disabled-hours="disabledHours"
              :disabled-minutes="disabledMinutes"
            />
          </el-form-item>
        </el-col>
        
        <el-col :span="12">
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input
              v-model="form.contactPhone"
              placeholder="请输入联系电话"
              maxlength="11"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="服务地址" prop="address">
        <el-input
          v-model="form.address"
          placeholder="请输入详细的服务地址"
          maxlength="200"
        />
      </el-form-item>
      
      <el-form-item label="服务描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请详细描述服务需求，以便工作人员更好地为您服务"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      
      <!-- 服务信息预览 -->
      <el-form-item v-if="selectedService" label="服务信息">
        <div class="service-preview">
          <el-card shadow="never">
            <div class="service-info">
              <div class="service-header">
                <span class="service-name">{{ selectedService.serviceName }}</span>
                <el-tag :type="getServiceTypeTagType(selectedService.serviceType)">
                  {{ getServiceTypeName(selectedService.serviceType) }}
                </el-tag>
              </div>
              <div class="service-details">
                <div class="detail-item">
                  <span class="label">服务费用：</span>
                  <span class="value price">¥{{ selectedService.price }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">预计时长：</span>
                  <span class="value">{{ selectedService.duration }}分钟</span>
                </div>
              </div>
              <div class="service-description">
                <div class="label">服务说明：</div>
                <div class="description-text">{{ selectedService.description }}</div>
              </div>
            </div>
          </el-card>
        </div>
      </el-form-item>
    </el-form>
    
    <div class="form-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        {{ isEdit ? '更新' : '创建' }}预约
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  type AppointmentRecord,
  type AppointmentForm,
  type AppointmentService,
  AppointmentType
} from '@/api/appointment'

// Props
interface Props {
  formData?: Partial<AppointmentRecord>
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  submit: []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)
const userLoading = ref(false)
const userOptions = ref<Array<{ id: number; username: string; phone: string }>>([])
const serviceOptions = ref<AppointmentService[]>([])

// 表单数据
const form = reactive<AppointmentForm>({
  userId: 0,
  serviceId: 0,
  appointmentTime: '',
  address: '',
  contactPhone: '',
  description: ''
})

// 计算属性
const isEdit = computed(() => !!props.formData?.id)

const selectedService = computed(() => {
  return serviceOptions.value.find(service => service.id === form.serviceId)
})

// 表单验证规则
const rules: FormRules = {
  userId: [
    { required: true, message: '请选择用户', trigger: 'change' }
  ],
  serviceId: [
    { required: true, message: '请选择服务类型', trigger: 'change' }
  ],
  appointmentTime: [
    { required: true, message: '请选择预约时间', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入服务地址', trigger: 'blur' },
    { min: 5, max: 200, message: '地址长度在 5 到 200 个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 监听表单数据变化
watch(() => props.formData, (newData) => {
  if (newData) {
    Object.assign(form, {
      userId: newData.userId || 0,
      serviceId: newData.serviceId || 0,
      appointmentTime: newData.appointmentTime || '',
      address: newData.address || '',
      contactPhone: newData.contactPhone || '',
      description: newData.description || ''
    })
    
    // 如果是编辑模式，预加载用户信息
    if (newData.userId) {
      loadUserInfo(newData.userId)
    }
  }
}, { immediate: true })

// 页面初始化
onMounted(() => {
  loadServices()
})

// 加载服务列表
const loadServices = async () => {
  try {
    // 使用模拟数据
    serviceOptions.value = [
      {
        id: 1,
        serviceName: '水管维修',
        serviceType: AppointmentType.MAINTENANCE,
        description: '专业水管维修服务，包括漏水修补、管道疏通、阀门更换等',
        price: 150,
        duration: 120,
        isActive: true,
        createdAt: '2024-01-01 00:00:00',
        updatedAt: '2024-01-01 00:00:00'
      },
      {
        id: 2,
        serviceName: '家庭保洁',
        serviceType: AppointmentType.CLEANING,
        description: '专业家庭清洁服务，包括全屋清洁、深度清洁、定期保洁等',
        price: 200,
        duration: 180,
        isActive: true,
        createdAt: '2024-01-01 00:00:00',
        updatedAt: '2024-01-01 00:00:00'
      },
      {
        id: 3,
        serviceName: '电器维修',
        serviceType: AppointmentType.MAINTENANCE,
        description: '家电维修服务，包括空调、洗衣机、冰箱、电视等家电维修',
        price: 300,
        duration: 150,
        isActive: true,
        createdAt: '2024-01-01 00:00:00',
        updatedAt: '2024-01-01 00:00:00'
      },
      {
        id: 4,
        serviceName: '快递代收',
        serviceType: AppointmentType.DELIVERY,
        description: '快递代收服务，安全可靠，支持重要文件和贵重物品代收',
        price: 10,
        duration: 30,
        isActive: true,
        createdAt: '2024-01-01 00:00:00',
        updatedAt: '2024-01-01 00:00:00'
      },
      {
        id: 5,
        serviceName: '安保巡逻',
        serviceType: AppointmentType.SECURITY,
        description: '专业安保巡逻服务，确保小区安全，定期巡查',
        price: 100,
        duration: 60,
        isActive: true,
        createdAt: '2024-01-01 00:00:00',
        updatedAt: '2024-01-01 00:00:00'
      }
    ]
  } catch (error) {
    console.error('加载服务列表失败:', error)
  }
}

// 搜索用户
const searchUsers = async (query: string) => {
  if (!query) {
    userOptions.value = []
    return
  }
  
  try {
    userLoading.value = true
    
    // 模拟API延迟
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 使用模拟数据
    const mockUsers = [
      { id: 1, username: '张三', phone: '13800138001' },
      { id: 2, username: '李四', phone: '13800138002' },
      { id: 3, username: '王五', phone: '13800138003' },
      { id: 4, username: '赵六', phone: '13800138004' },
      { id: 5, username: '孙七', phone: '13800138005' },
      { id: 6, username: '周八', phone: '13800138006' },
      { id: 7, username: '吴九', phone: '13800138007' },
      { id: 8, username: '郑十', phone: '13800138008' }
    ]
    
    // 根据查询条件过滤用户
    userOptions.value = mockUsers.filter(user => 
      user.username.includes(query) || user.phone.includes(query)
    )
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    userLoading.value = false
  }
}

// 加载用户信息
const loadUserInfo = async (userId: number) => {
  try {
    // 模拟根据ID获取用户信息
    const mockUsers = [
      { id: 1, username: '张三', phone: '13800138001' },
      { id: 2, username: '李四', phone: '13800138002' },
      { id: 3, username: '王五', phone: '13800138003' },
      { id: 4, username: '赵六', phone: '13800138004' },
      { id: 5, username: '孙七', phone: '13800138005' }
    ]
    
    const user = mockUsers.find(u => u.id === userId)
    if (user) {
      userOptions.value = [user]
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 服务变更处理
const handleServiceChange = (serviceId: number) => {
  const service = serviceOptions.value.find(s => s.id === serviceId)
  if (service && !form.contactPhone && userOptions.value.length > 0) {
    const selectedUser = userOptions.value.find(u => u.id === form.userId)
    if (selectedUser) {
      form.contactPhone = selectedUser.phone
    }
  }
}

// 禁用日期
const disabledDate = (time: Date) => {
  // 禁用过去的日期
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

// 禁用小时
const disabledHours = () => {
  const now = new Date()
  const selectedDate = new Date(form.appointmentTime)
  
  // 如果是今天，禁用已过去的小时
  if (selectedDate.toDateString() === now.toDateString()) {
    const currentHour = now.getHours()
    return Array.from({ length: currentHour }, (_, i) => i)
  }
  
  // 工作时间：8:00-18:00
  return [0, 1, 2, 3, 4, 5, 6, 7, 19, 20, 21, 22, 23]
}

// 禁用分钟
const disabledMinutes = (hour: number) => {
  const now = new Date()
  const selectedDate = new Date(form.appointmentTime)
  
  // 如果是今天且是当前小时，禁用已过去的分钟
  if (selectedDate.toDateString() === now.toDateString() && hour === now.getHours()) {
    const currentMinute = now.getMinutes()
    return Array.from({ length: currentMinute }, (_, i) => i)
  }
  
  return []
}

// 获取服务类型名称
const getServiceTypeName = (type: AppointmentType) => {
  const typeMap = {
    [AppointmentType.MAINTENANCE]: '维修服务',
    [AppointmentType.CLEANING]: '保洁服务',
    [AppointmentType.SECURITY]: '安保服务',
    [AppointmentType.DELIVERY]: '快递代收',
    [AppointmentType.OTHER]: '其他服务'
  }
  return typeMap[type] || '未知'
}

// 获取服务类型标签类型
const getServiceTypeTagType = (type: AppointmentType) => {
  const typeMap = {
    [AppointmentType.MAINTENANCE]: 'danger',
    [AppointmentType.CLEANING]: 'success',
    [AppointmentType.SECURITY]: 'warning',
    [AppointmentType.DELIVERY]: 'info',
    [AppointmentType.OTHER]: ''
  }
  return typeMap[type] || ''
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success(isEdit.value ? '预约更新成功' : '预约创建成功')
    emit('submit')
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  emit('submit')
}
</script>

<style scoped lang="scss">
.appointment-form {
  .service-preview {
    width: 100%;
    
    .service-info {
      .service-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 16px;
        
        .service-name {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
        }
      }
      
      .service-details {
        display: flex;
        gap: 24px;
        margin-bottom: 16px;
        
        .detail-item {
          display: flex;
          align-items: center;
          
          .label {
            color: #606266;
            margin-right: 8px;
          }
          
          .value {
            font-weight: 500;
            color: #303133;
            
            &.price {
              color: #f56c6c;
              font-size: 16px;
            }
          }
        }
      }
      
      .service-description {
        .label {
          color: #606266;
          margin-bottom: 8px;
        }
        
        .description-text {
          color: #303133;
          line-height: 1.6;
          padding: 12px;
          background-color: #f5f7fa;
          border-radius: 4px;
          border-left: 4px solid #409eff;
        }
      }
    }
  }
  
  .form-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #ebeef5;
  }
}

:deep(.el-card__body) {
  padding: 16px;
}
</style>
