<template>
  <div class="worker-assign">
    <div class="appointment-info">
      <h4>预约信息</h4>
      <el-descriptions :column="2" size="small" border>
        <el-descriptions-item label="预约ID">
          {{ appointment.id }}
        </el-descriptions-item>
        <el-descriptions-item label="服务名称">
          {{ appointment.serviceName }}
        </el-descriptions-item>
        <el-descriptions-item label="用户">
          {{ appointment.username }}
        </el-descriptions-item>
        <el-descriptions-item label="预约时间">
          {{ formatDateTime(appointment.appointmentTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="服务地址" :span="2">
          {{ appointment.address }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
    
    <div class="worker-selection">
      <h4>选择工作人员</h4>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="工作人员" prop="workerId">
          <el-select
            v-model="form.workerId"
            placeholder="请选择工作人员"
            style="width: 100%"
            filterable
            @change="handleWorkerChange"
          >
            <el-option
              v-for="worker in availableWorkers"
              :key="worker.id"
              :label="`${worker.name} (${worker.phone})`"
              :value="worker.id"
            >
              <div class="worker-option">
                <div class="worker-info">
                  <span class="worker-name">{{ worker.name }}</span>
                  <span class="worker-phone">{{ worker.phone }}</span>
                </div>
                <div class="worker-meta">
                  <el-tag :type="getSkillTagType(worker.skills)" size="small">
                    {{ worker.skills.join(', ') }}
                  </el-tag>
                  <span class="worker-rating">
                    <el-icon><Star /></el-icon>
                    {{ worker.rating }}
                  </span>
                </div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <!-- 选中工作人员的详细信息 -->
        <div v-if="selectedWorker" class="worker-detail">
          <el-card shadow="never">
            <div class="worker-profile">
              <div class="worker-header">
                <div class="worker-basic">
                  <h5>{{ selectedWorker.name }}</h5>
                  <div class="worker-tags">
                    <el-tag 
                      v-for="skill in selectedWorker.skills" 
                      :key="skill" 
                      :type="getSkillTagType([skill])" 
                      size="small"
                    >
                      {{ skill }}
                    </el-tag>
                  </div>
                </div>
                <div class="worker-stats">
                  <div class="stat-item">
                    <span class="stat-label">评分</span>
                    <span class="stat-value">
                      <el-icon><Star /></el-icon>
                      {{ selectedWorker.rating }}
                    </span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">完成订单</span>
                    <span class="stat-value">{{ selectedWorker.completedOrders }}</span>
                  </div>
                </div>
              </div>
              
              <div class="worker-info-grid">
                <div class="info-item">
                  <span class="info-label">联系电话：</span>
                  <span class="info-value">{{ selectedWorker.phone }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">工作状态：</span>
                  <el-tag :type="selectedWorker.status === 'available' ? 'success' : 'warning'" size="small">
                    {{ getStatusName(selectedWorker.status) }}
                  </el-tag>
                </div>
                <div class="info-item">
                  <span class="info-label">当前任务：</span>
                  <span class="info-value">{{ selectedWorker.currentTasks }}个</span>
                </div>
                <div class="info-item">
                  <span class="info-label">服务区域：</span>
                  <span class="info-value">{{ selectedWorker.serviceArea.join(', ') }}</span>
                </div>
              </div>
              
              <div v-if="selectedWorker.description" class="worker-description">
                <div class="description-label">个人简介：</div>
                <div class="description-text">{{ selectedWorker.description }}</div>
              </div>
            </div>
          </el-card>
        </div>
        
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="可以添加特殊要求或备注信息"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
    </div>
    
    <div class="form-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        确认分配
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { AppointmentRecord } from '@/api/appointment'

// Props
interface Props {
  appointment: Partial<AppointmentRecord>
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  submit: []
}>()

// 工作人员接口
interface Worker {
  id: number
  name: string
  phone: string
  skills: string[]
  rating: number
  completedOrders: number
  status: 'available' | 'busy'
  currentTasks: number
  serviceArea: string[]
  description?: string
}

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)
const availableWorkers = ref<Worker[]>([])

// 表单数据
const form = reactive({
  workerId: 0,
  remark: ''
})

// 表单验证规则
const rules: FormRules = {
  workerId: [
    { required: true, message: '请选择工作人员', trigger: 'change' }
  ]
}

// 计算属性
const selectedWorker = computed(() => {
  return availableWorkers.value.find(worker => worker.id === form.workerId)
})

// 页面初始化
onMounted(() => {
  loadAvailableWorkers()
})

// 加载可用工作人员
const loadAvailableWorkers = async () => {
  try {
    // 使用模拟数据
    const mockWorkers: Worker[] = [
      {
        id: 1,
        name: '王师傅',
        phone: '13900139001',
        skills: ['水管维修', '电器维修', '家具安装'],
        rating: 4.8,
        completedOrders: 156,
        status: 'available',
        currentTasks: 2,
        serviceArea: ['阳光小区', '花园小区', '绿城小区'],
        description: '从事维修行业10年，经验丰富，服务态度好，深受业主好评。'
      },
      {
        id: 2,
        name: '刘师傅',
        phone: '13900139002',
        skills: ['电器维修', '空调维修', '热水器维修'],
        rating: 4.9,
        completedOrders: 203,
        status: 'available',
        currentTasks: 1,
        serviceArea: ['阳光小区', '星河小区'],
        description: '专业电器维修师傅，持有相关资格证书，技术精湛。'
      },
      {
        id: 3,
        name: '陈师傅',
        phone: '13900139003',
        skills: ['快递代收', '包裹配送', '文件传递'],
        rating: 4.7,
        completedOrders: 89,
        status: 'available',
        currentTasks: 0,
        serviceArea: ['阳光小区', '花园小区', '绿城小区', '星河小区'],
        description: '负责小区快递代收服务，工作认真负责，从不出错。'
      },
      {
        id: 4,
        name: '李师傅',
        phone: '13900139004',
        skills: ['家庭保洁', '深度清洁', '开荒保洁'],
        rating: 4.6,
        completedOrders: 134,
        status: 'busy',
        currentTasks: 3,
        serviceArea: ['阳光小区', '绿城小区'],
        description: '专业保洁人员，使用环保清洁用品，服务细致周到。'
      },
      {
        id: 5,
        name: '张师傅',
        phone: '13900139005',
        skills: ['安保巡逻', '门禁维护', '监控检查'],
        rating: 4.5,
        completedOrders: 67,
        status: 'available',
        currentTasks: 1,
        serviceArea: ['阳光小区'],
        description: '退伍军人，责任心强，安保经验丰富。'
      }
    ]
    
    // 根据服务类型过滤合适的工作人员
    availableWorkers.value = mockWorkers.filter(worker => {
      // 这里可以根据预约的服务类型来过滤工作人员
      return worker.status === 'available' || worker.currentTasks < 3
    })
    
  } catch (error) {
    console.error('加载工作人员列表失败:', error)
    ElMessage.error('加载工作人员列表失败')
  }
}

// 工作人员变更处理
const handleWorkerChange = (workerId: number) => {
  const worker = availableWorkers.value.find(w => w.id === workerId)
  if (worker) {
    console.log('选择工作人员:', worker.name)
  }
}

// 获取技能标签类型
const getSkillTagType = (skills: string[]) => {
  if (skills.some(skill => skill.includes('维修'))) return 'danger'
  if (skills.some(skill => skill.includes('保洁'))) return 'success'
  if (skills.some(skill => skill.includes('安保'))) return 'warning'
  if (skills.some(skill => skill.includes('快递'))) return 'info'
  return 'primary'
}

// 获取状态名称
const getStatusName = (status: string) => {
  const statusMap: Record<string, string> = {
    'available': '空闲中',
    'busy': '忙碌中'
  }
  return statusMap[status] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('工作人员分配成功')
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
.worker-assign {
  .appointment-info {
    margin-bottom: 24px;
    
    h4 {
      margin: 0 0 16px 0;
      color: #303133;
      font-size: 16px;
      font-weight: 600;
      border-left: 4px solid #409eff;
      padding-left: 12px;
    }
  }
  
  .worker-selection {
    margin-bottom: 24px;
    
    h4 {
      margin: 0 0 16px 0;
      color: #303133;
      font-size: 16px;
      font-weight: 600;
      border-left: 4px solid #409eff;
      padding-left: 12px;
    }
    
    .worker-detail {
      margin-top: 16px;
      
      .worker-profile {
        .worker-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 16px;
          
          .worker-basic {
            flex: 1;
            
            h5 {
              margin: 0 0 8px 0;
              color: #303133;
              font-size: 16px;
              font-weight: 600;
            }
            
            .worker-tags {
              display: flex;
              gap: 6px;
              flex-wrap: wrap;
            }
          }
          
          .worker-stats {
            display: flex;
            gap: 16px;
            
            .stat-item {
              display: flex;
              flex-direction: column;
              align-items: center;
              
              .stat-label {
                font-size: 12px;
                color: #909399;
                margin-bottom: 4px;
              }
              
              .stat-value {
                font-size: 14px;
                font-weight: 600;
                color: #303133;
                display: flex;
                align-items: center;
                gap: 2px;
                
                .el-icon {
                  color: #f7ba2a;
                }
              }
            }
          }
        }
        
        .worker-info-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 12px;
          margin-bottom: 16px;
          
          .info-item {
            display: flex;
            align-items: center;
            
            .info-label {
              color: #606266;
              font-size: 14px;
              margin-right: 8px;
            }
            
            .info-value {
              color: #303133;
              font-size: 14px;
            }
          }
        }
        
        .worker-description {
          .description-label {
            color: #606266;
            font-size: 14px;
            margin-bottom: 8px;
          }
          
          .description-text {
            color: #303133;
            font-size: 14px;
            line-height: 1.6;
            padding: 12px;
            background-color: #f5f7fa;
            border-radius: 4px;
            border-left: 4px solid #409eff;
          }
        }
      }
    }
  }
  
  .form-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 24px;
    border-top: 1px solid #ebeef5;
  }
}

:deep(.el-select-dropdown__item) {
  height: auto;
  padding: 8px 20px;
  
  .worker-option {
    width: 100%;
    
    .worker-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;
      
      .worker-name {
        font-weight: 500;
        color: #303133;
      }
      
      .worker-phone {
        font-size: 12px;
        color: #909399;
      }
    }
    
    .worker-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .worker-rating {
        display: flex;
        align-items: center;
        gap: 2px;
        font-size: 12px;
        color: #f7ba2a;
        
        .el-icon {
          font-size: 12px;
        }
      }
    }
  }
}

:deep(.el-card__body) {
  padding: 16px;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
}
</style>
