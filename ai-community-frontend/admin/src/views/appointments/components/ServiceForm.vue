<template>
  <div class="service-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="服务名称" prop="serviceName">
            <el-input
              v-model="form.serviceName"
              placeholder="请输入服务名称"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
        
        <el-col :span="12">
          <el-form-item label="服务类型" prop="serviceType">
            <el-select
              v-model="form.serviceType"
              placeholder="选择服务类型"
              style="width: 100%"
            >
              <el-option
                v-for="type in serviceTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="服务价格" prop="price">
            <el-input-number
              v-model="form.price"
              :min="0"
              :max="9999"
              :precision="2"
              controls-position="right"
              style="width: 100%"
            />
            <span style="margin-left: 8px; color: #909399;">元</span>
          </el-form-item>
        </el-col>
        
        <el-col :span="12">
          <el-form-item label="预计时长" prop="duration">
            <el-input-number
              v-model="form.duration"
              :min="15"
              :max="480"
              :step="15"
              controls-position="right"
              style="width: 100%"
            />
            <span style="margin-left: 8px; color: #909399;">分钟</span>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="服务状态" prop="isActive">
        <el-switch
          v-model="form.isActive"
          active-text="启用"
          inactive-text="禁用"
        />
        <div style="margin-top: 8px; font-size: 12px; color: #909399;">
          禁用后用户将无法预约此服务
        </div>
      </el-form-item>
      
      <el-form-item label="服务描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请详细描述服务内容、服务范围、注意事项等"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      
      <!-- 服务预览 -->
      <el-form-item label="服务预览">
        <div class="service-preview">
          <el-card shadow="never">
            <div class="preview-header">
              <div class="service-info">
                <h4>{{ form.serviceName || '服务名称' }}</h4>
                <el-tag :type="getServiceTypeTagType(form.serviceType)">
                  {{ getServiceTypeName(form.serviceType) }}
                </el-tag>
              </div>
              <div class="service-price">
                <span class="price">¥{{ form.price || 0 }}</span>
              </div>
            </div>
            
            <div class="preview-content">
              <div class="info-item">
                <span class="label">预计时长：</span>
                <span class="value">{{ form.duration || 0 }}分钟</span>
              </div>
              <div class="info-item">
                <span class="label">服务状态：</span>
                <el-tag :type="form.isActive ? 'success' : 'danger'" size="small">
                  {{ form.isActive ? '启用中' : '已禁用' }}
                </el-tag>
              </div>
            </div>
            
            <div v-if="form.description" class="preview-description">
              <div class="description-label">服务说明：</div>
              <div class="description-text">{{ form.description }}</div>
            </div>
          </el-card>
        </div>
      </el-form-item>
    </el-form>
    
    <div class="form-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        {{ isEdit ? '更新' : '创建' }}服务
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  type AppointmentService,
  type ServiceForm,
  AppointmentType
} from '@/api/appointment'

// Props
interface Props {
  formData?: Partial<AppointmentService>
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  submit: []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)

// 表单数据
const form = reactive<ServiceForm>({
  serviceName: '',
  serviceType: AppointmentType.OTHER,
  description: '',
  price: 0,
  duration: 60,
  isActive: true
})

// 服务类型选项
const serviceTypes = [
  { value: AppointmentType.MAINTENANCE, label: '维修服务' },
  { value: AppointmentType.CLEANING, label: '保洁服务' },
  { value: AppointmentType.SECURITY, label: '安保服务' },
  { value: AppointmentType.DELIVERY, label: '快递代收' },
  { value: AppointmentType.OTHER, label: '其他服务' }
]

// 计算属性
const isEdit = computed(() => !!props.formData?.id)

// 表单验证规则
const rules: FormRules = {
  serviceName: [
    { required: true, message: '请输入服务名称', trigger: 'blur' },
    { min: 2, max: 50, message: '服务名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  serviceType: [
    { required: true, message: '请选择服务类型', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入服务价格', trigger: 'blur' },
    { type: 'number', min: 0, max: 9999, message: '价格范围在 0 到 9999 元', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入预计时长', trigger: 'blur' },
    { type: 'number', min: 15, max: 480, message: '时长范围在 15 到 480 分钟', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入服务描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

// 监听表单数据变化
watch(() => props.formData, (newData) => {
  if (newData) {
    Object.assign(form, {
      serviceName: newData.serviceName || '',
      serviceType: newData.serviceType || AppointmentType.OTHER,
      description: newData.description || '',
      price: newData.price || 0,
      duration: newData.duration || 60,
      isActive: newData.isActive !== undefined ? newData.isActive : true
    })
  }
}, { immediate: true })

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
    
    ElMessage.success(isEdit.value ? '服务更新成功' : '服务创建成功')
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
.service-form {
  .service-preview {
    width: 100%;
    
    .preview-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;
      
      .service-info {
        flex: 1;
        
        h4 {
          margin: 0 0 8px 0;
          color: #303133;
          font-size: 16px;
          font-weight: 600;
        }
      }
      
      .service-price {
        .price {
          font-size: 20px;
          font-weight: 600;
          color: #f56c6c;
        }
      }
    }
    
    .preview-content {
      display: flex;
      gap: 24px;
      margin-bottom: 16px;
      
      .info-item {
        display: flex;
        align-items: center;
        
        .label {
          color: #606266;
          margin-right: 8px;
        }
        
        .value {
          font-weight: 500;
          color: #303133;
        }
      }
    }
    
    .preview-description {
      .description-label {
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

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}
</style>
