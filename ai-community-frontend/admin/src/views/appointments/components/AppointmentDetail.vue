<template>
  <div class="appointment-detail">
    <div class="detail-header">
      <div class="appointment-title">
        <h3>预约详情 #{{ appointment.id }}</h3>
        <div class="appointment-meta">
          <el-tag :type="getStatusTagType(appointment.status)">
            {{ getStatusName(appointment.status) }}
          </el-tag>
          <el-tag :type="getServiceTypeTagType(appointment.serviceType)" style="margin-left: 8px;">
            {{ getServiceTypeName(appointment.serviceType) }}
          </el-tag>
        </div>
      </div>
    </div>
    
    <div class="detail-content">
      <!-- 基本信息 -->
      <div class="info-section">
        <h4>基本信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预约ID">
            {{ appointment.id }}
          </el-descriptions-item>
          <el-descriptions-item label="预约状态">
            <el-tag :type="getStatusTagType(appointment.status)">
              {{ getStatusName(appointment.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="服务名称">
            {{ appointment.serviceName }}
          </el-descriptions-item>
          <el-descriptions-item label="服务类型">
            <el-tag :type="getServiceTypeTagType(appointment.serviceType)">
              {{ getServiceTypeName(appointment.serviceType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预约时间">
            {{ formatDateTime(appointment.appointmentTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="服务费用">
            <span class="price">¥{{ appointment.price }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(appointment.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(appointment.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 用户信息 -->
      <div class="info-section">
        <h4>用户信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">
            {{ appointment.username }}
          </el-descriptions-item>
          <el-descriptions-item label="用户手机">
            {{ appointment.userPhone }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ appointment.contactPhone }}
          </el-descriptions-item>
          <el-descriptions-item label="服务地址" :span="2">
            {{ appointment.address }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 工作人员信息 -->
      <div v-if="appointment.workerName" class="info-section">
        <h4>工作人员信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工作人员">
            {{ appointment.workerName }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ appointment.workerPhone }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 服务描述 -->
      <div v-if="appointment.description" class="info-section">
        <h4>服务描述</h4>
        <div class="description-content">
          {{ appointment.description }}
        </div>
      </div>
      
      <!-- 完成信息 -->
      <div v-if="appointment.status === 3" class="info-section">
        <h4>完成信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="完成时间">
            {{ formatDateTime(appointment.completedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="用户评分">
            <el-rate
              v-if="appointment.rating"
              v-model="appointment.rating"
              disabled
              show-score
              text-color="#ff9900"
            />
            <span v-else class="no-rating">暂未评分</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="appointment.feedback" label="用户反馈" :span="2">
            <div class="feedback-content">
              {{ appointment.feedback }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 取消信息 -->
      <div v-if="appointment.status === 4" class="info-section">
        <h4>取消信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="取消时间">
            {{ formatDateTime(appointment.cancelledAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="取消原因" :span="2">
            <div class="cancel-reason">
              {{ appointment.cancelReason || '无' }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 操作记录 -->
      <div class="info-section">
        <h4>操作记录</h4>
        <el-timeline>
          <el-timeline-item
            v-for="record in operationRecords"
            :key="record.id"
            :timestamp="record.timestamp"
            :type="record.type"
          >
            <div class="operation-content">
              <div class="operation-title">{{ record.title }}</div>
              <div v-if="record.description" class="operation-description">
                {{ record.description }}
              </div>
              <div v-if="record.operator" class="operation-operator">
                操作人：{{ record.operator }}
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
      
      <!-- 移动端预览 -->
      <div class="info-section">
        <h4>移动端预览</h4>
        <div class="mobile-preview">
          <div class="mobile-header">
            <div class="mobile-title">
              <span class="title-text">预约服务详情</span>
              <el-tag :type="getStatusTagType(appointment.status)" size="small">
                {{ getStatusName(appointment.status) }}
              </el-tag>
            </div>
          </div>
          <div class="mobile-content">
            <div class="mobile-item">
              <div class="item-label">服务名称</div>
              <div class="item-value">{{ appointment.serviceName }}</div>
            </div>
            <div class="mobile-item">
              <div class="item-label">预约时间</div>
              <div class="item-value">{{ formatDateTime(appointment.appointmentTime) }}</div>
            </div>
            <div class="mobile-item">
              <div class="item-label">服务地址</div>
              <div class="item-value">{{ appointment.address }}</div>
            </div>
            <div class="mobile-item">
              <div class="item-label">联系电话</div>
              <div class="item-value">{{ appointment.contactPhone }}</div>
            </div>
            <div class="mobile-item">
              <div class="item-label">服务费用</div>
              <div class="item-value price">¥{{ appointment.price }}</div>
            </div>
            <div v-if="appointment.workerName" class="mobile-item">
              <div class="item-label">服务人员</div>
              <div class="item-value">{{ appointment.workerName }} ({{ appointment.workerPhone }})</div>
            </div>
            <div v-if="appointment.description" class="mobile-item">
              <div class="item-label">服务说明</div>
              <div class="item-value description">{{ appointment.description }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  type AppointmentRecord,
  AppointmentType,
  AppointmentStatus
} from '@/api/appointment'

// Props
interface Props {
  appointment: Partial<AppointmentRecord>
}

const props = defineProps<Props>()

// 操作记录
const operationRecords = computed(() => {
  const records = [
    {
      id: 1,
      timestamp: props.appointment.createdAt,
      type: 'primary',
      title: '预约创建',
      description: `用户 ${props.appointment.username} 创建了预约`,
      operator: '系统'
    }
  ]
  
  if (props.appointment.status >= AppointmentStatus.CONFIRMED) {
    records.push({
      id: 2,
      timestamp: props.appointment.updatedAt,
      type: 'success',
      title: '预约确认',
      description: '预约已确认，等待分配工作人员',
      operator: '管理员'
    })
  }
  
  if (props.appointment.workerName) {
    records.push({
      id: 3,
      timestamp: props.appointment.updatedAt,
      type: 'info',
      title: '分配工作人员',
      description: `已分配工作人员：${props.appointment.workerName}`,
      operator: '管理员'
    })
  }
  
  if (props.appointment.status === AppointmentStatus.IN_PROGRESS) {
    records.push({
      id: 4,
      timestamp: props.appointment.updatedAt,
      type: 'warning',
      title: '开始服务',
      description: '工作人员已开始提供服务',
      operator: props.appointment.workerName || '工作人员'
    })
  }
  
  if (props.appointment.status === AppointmentStatus.COMPLETED) {
    records.push({
      id: 5,
      timestamp: props.appointment.completedAt,
      type: 'success',
      title: '服务完成',
      description: '服务已完成',
      operator: props.appointment.workerName || '工作人员'
    })
    
    if (props.appointment.rating) {
      records.push({
        id: 6,
        timestamp: props.appointment.completedAt,
        type: 'primary',
        title: '用户评价',
        description: `用户给出了 ${props.appointment.rating} 星评价`,
        operator: props.appointment.username
      })
    }
  }
  
  if (props.appointment.status === AppointmentStatus.CANCELLED) {
    records.push({
      id: 7,
      timestamp: props.appointment.cancelledAt,
      type: 'danger',
      title: '预约取消',
      description: props.appointment.cancelReason || '预约已取消',
      operator: '管理员'
    })
  }
  
  return records.sort((a, b) => new Date(a.timestamp || '').getTime() - new Date(b.timestamp || '').getTime())
})

// 获取服务类型名称
const getServiceTypeName = (type?: AppointmentType) => {
  const typeMap = {
    [AppointmentType.MAINTENANCE]: '维修服务',
    [AppointmentType.CLEANING]: '保洁服务',
    [AppointmentType.SECURITY]: '安保服务',
    [AppointmentType.DELIVERY]: '快递代收',
    [AppointmentType.OTHER]: '其他服务'
  }
  return typeMap[type || AppointmentType.OTHER] || '未知'
}

// 获取服务类型标签类型
const getServiceTypeTagType = (type?: AppointmentType) => {
  const typeMap = {
    [AppointmentType.MAINTENANCE]: 'danger',
    [AppointmentType.CLEANING]: 'success',
    [AppointmentType.SECURITY]: 'warning',
    [AppointmentType.DELIVERY]: 'info',
    [AppointmentType.OTHER]: ''
  }
  return typeMap[type || AppointmentType.OTHER] || ''
}

// 获取状态名称
const getStatusName = (status?: AppointmentStatus) => {
  const statusMap = {
    [AppointmentStatus.PENDING]: '待处理',
    [AppointmentStatus.CONFIRMED]: '已确认',
    [AppointmentStatus.IN_PROGRESS]: '进行中',
    [AppointmentStatus.COMPLETED]: '已完成',
    [AppointmentStatus.CANCELLED]: '已取消'
  }
  return statusMap[status || AppointmentStatus.PENDING] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status?: AppointmentStatus) => {
  const statusMap = {
    [AppointmentStatus.PENDING]: 'warning',
    [AppointmentStatus.CONFIRMED]: 'primary',
    [AppointmentStatus.IN_PROGRESS]: 'info',
    [AppointmentStatus.COMPLETED]: 'success',
    [AppointmentStatus.CANCELLED]: 'danger'
  }
  return statusMap[status || AppointmentStatus.PENDING] || ''
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
</script>

<style scoped lang="scss">
.appointment-detail {
  .detail-header {
    margin-bottom: 24px;
    
    .appointment-title {
      h3 {
        margin: 0 0 12px 0;
        color: #303133;
        font-size: 20px;
        line-height: 1.4;
      }
      
      .appointment-meta {
        display: flex;
        gap: 8px;
        align-items: center;
      }
    }
  }
  
  .detail-content {
    .info-section {
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
    
    .description-content {
      padding: 16px;
      background-color: #fafafa;
      border-radius: 6px;
      border: 1px solid #ebeef5;
      line-height: 1.6;
      color: #606266;
    }
    
    .feedback-content {
      padding: 12px;
      background-color: #f0f9ff;
      border-radius: 4px;
      border-left: 4px solid #409eff;
      line-height: 1.6;
      color: #303133;
    }
    
    .cancel-reason {
      padding: 12px;
      background-color: #fef0f0;
      border-radius: 4px;
      border-left: 4px solid #f56c6c;
      line-height: 1.6;
      color: #f56c6c;
    }
    
    .no-rating {
      color: #c0c4cc;
      font-style: italic;
    }
    
    .price {
      font-weight: 600;
      color: #f56c6c;
      font-size: 16px;
    }
    
    .operation-content {
      .operation-title {
        font-weight: 500;
        color: #303133;
        margin-bottom: 4px;
      }
      
      .operation-description {
        color: #606266;
        font-size: 14px;
        margin-bottom: 4px;
      }
      
      .operation-operator {
        color: #909399;
        font-size: 12px;
      }
    }
    
    .mobile-preview {
      max-width: 375px;
      margin: 0 auto;
      border: 1px solid #dcdfe6;
      border-radius: 12px;
      overflow: hidden;
      background: #fff;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      
      .mobile-header {
        padding: 16px;
        border-bottom: 1px solid #f0f0f0;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        
        .mobile-title {
          display: flex;
          align-items: center;
          justify-content: space-between;
          
          .title-text {
            font-size: 16px;
            font-weight: 600;
          }
        }
      }
      
      .mobile-content {
        padding: 16px;
        
        .mobile-item {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          padding: 12px 0;
          border-bottom: 1px solid #f0f0f0;
          
          &:last-child {
            border-bottom: none;
          }
          
          .item-label {
            font-size: 14px;
            color: #646566;
            width: 80px;
            flex-shrink: 0;
          }
          
          .item-value {
            font-size: 14px;
            color: #323233;
            flex: 1;
            text-align: right;
            
            &.price {
              color: #ff6b35;
              font-weight: 600;
            }
            
            &.description {
              text-align: left;
              line-height: 1.5;
            }
          }
        }
      }
    }
  }
}

:deep(.el-descriptions__label) {
  font-weight: 500;
}

:deep(.el-timeline-item__timestamp) {
  font-size: 12px;
}
</style>
