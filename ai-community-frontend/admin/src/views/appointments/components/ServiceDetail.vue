<template>
  <div class="service-detail">
    <div class="detail-header">
      <div class="service-title">
        <h3>{{ service.serviceName }}</h3>
        <div class="service-meta">
          <el-tag :type="getServiceTypeTagType(service.serviceType)">
            {{ getServiceTypeName(service.serviceType) }}
          </el-tag>
          <el-tag :type="service.isActive ? 'success' : 'danger'" style="margin-left: 8px;">
            {{ service.isActive ? '启用中' : '已禁用' }}
          </el-tag>
        </div>
      </div>
      <div class="service-price">
        <span class="price">¥{{ service.price }}</span>
      </div>
    </div>
    
    <div class="detail-content">
      <!-- 基本信息 -->
      <div class="info-section">
        <h4>基本信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="服务ID">
            {{ service.id }}
          </el-descriptions-item>
          <el-descriptions-item label="服务状态">
            <el-tag :type="service.isActive ? 'success' : 'danger'">
              {{ service.isActive ? '启用中' : '已禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="服务名称">
            {{ service.serviceName }}
          </el-descriptions-item>
          <el-descriptions-item label="服务类型">
            <el-tag :type="getServiceTypeTagType(service.serviceType)">
              {{ getServiceTypeName(service.serviceType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="服务价格">
            <span class="price">¥{{ service.price }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="预计时长">
            {{ service.duration }}分钟
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(service.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(service.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 服务描述 -->
      <div class="info-section">
        <h4>服务描述</h4>
        <div class="description-content">
          {{ service.description }}
        </div>
      </div>
      
      <!-- 服务统计 -->
      <div class="info-section">
        <h4>服务统计</h4>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ mockStats.totalOrders }}</div>
              <div class="stat-label">总预约数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ mockStats.completedOrders }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ mockStats.averageRating.toFixed(1) }}</div>
              <div class="stat-label">平均评分</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">¥{{ mockStats.totalRevenue }}</div>
              <div class="stat-label">总收入</div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <!-- 最近预约 -->
      <div class="info-section">
        <h4>最近预约</h4>
        <el-table :data="mockRecentOrders" size="small">
          <el-table-column prop="id" label="预约ID" width="80" />
          <el-table-column prop="username" label="用户" width="100" />
          <el-table-column prop="appointmentTime" label="预约时间" width="150">
            <template #default="{ row }">
              {{ formatDateTime(row.appointmentTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" size="small">
                {{ getStatusName(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="address" label="地址" min-width="200">
            <template #default="{ row }">
              {{ truncateText(row.address, 30) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 移动端预览 -->
      <div class="info-section">
        <h4>移动端预览</h4>
        <div class="mobile-preview">
          <div class="mobile-header">
            <div class="mobile-title">
              <span class="title-text">{{ service.serviceName }}</span>
              <div class="mobile-price">¥{{ service.price }}</div>
            </div>
            <div class="mobile-type">
              <span class="type-tag">{{ getServiceTypeName(service.serviceType) }}</span>
            </div>
          </div>
          <div class="mobile-content">
            <div class="mobile-info">
              <div class="info-item">
                <span class="info-icon">⏰</span>
                <span class="info-text">预计时长：{{ service.duration }}分钟</span>
              </div>
              <div class="info-item">
                <span class="info-icon">⭐</span>
                <span class="info-text">评分：{{ mockStats.averageRating.toFixed(1) }}分</span>
              </div>
              <div class="info-item">
                <span class="info-icon">📊</span>
                <span class="info-text">已服务：{{ mockStats.completedOrders }}次</span>
              </div>
            </div>
            <div class="mobile-description">
              <div class="description-title">
                <span class="desc-icon">📝</span>
                <span>服务说明</span>
              </div>
              <div class="description-text">{{ service.description }}</div>
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
  type AppointmentService,
  AppointmentType,
  AppointmentStatus
} from '@/api/appointment'

// Props
interface Props {
  service: Partial<AppointmentService>
}

const props = defineProps<Props>()

// 模拟统计数据
const mockStats = computed(() => ({
  totalOrders: Math.floor(Math.random() * 100) + 20,
  completedOrders: Math.floor(Math.random() * 80) + 15,
  averageRating: Math.random() * 2 + 3.5, // 3.5-5.5 之间
  totalRevenue: Math.floor(Math.random() * 10000) + 2000
}))

// 模拟最近预约数据
const mockRecentOrders = computed(() => [
  {
    id: 1,
    username: '张三',
    appointmentTime: '2024-01-20 14:00:00',
    status: AppointmentStatus.PENDING,
    address: '阳光小区1栋2单元301室'
  },
  {
    id: 2,
    username: '李四',
    appointmentTime: '2024-01-19 16:00:00',
    status: AppointmentStatus.COMPLETED,
    address: '阳光小区2栋1单元201室'
  },
  {
    id: 3,
    username: '王五',
    appointmentTime: '2024-01-18 10:00:00',
    status: AppointmentStatus.IN_PROGRESS,
    address: '阳光小区3栋3单元101室'
  }
])

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
const getStatusName = (status: AppointmentStatus) => {
  const statusMap = {
    [AppointmentStatus.PENDING]: '待处理',
    [AppointmentStatus.CONFIRMED]: '已确认',
    [AppointmentStatus.IN_PROGRESS]: '进行中',
    [AppointmentStatus.COMPLETED]: '已完成',
    [AppointmentStatus.CANCELLED]: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status: AppointmentStatus) => {
  const statusMap = {
    [AppointmentStatus.PENDING]: 'warning',
    [AppointmentStatus.CONFIRMED]: 'primary',
    [AppointmentStatus.IN_PROGRESS]: 'info',
    [AppointmentStatus.COMPLETED]: 'success',
    [AppointmentStatus.CANCELLED]: 'danger'
  }
  return statusMap[status] || ''
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

// 截断文本
const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}
</script>

<style scoped lang="scss">
.service-detail {
  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    
    .service-title {
      flex: 1;
      
      h3 {
        margin: 0 0 12px 0;
        color: #303133;
        font-size: 20px;
        line-height: 1.4;
      }
      
      .service-meta {
        display: flex;
        gap: 8px;
        align-items: center;
      }
    }
    
    .service-price {
      .price {
        font-size: 24px;
        font-weight: 600;
        color: #f56c6c;
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
    
    .price {
      font-weight: 600;
      color: #f56c6c;
      font-size: 16px;
    }
    
    .stat-card {
      text-align: center;
      padding: 20px;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      
      .stat-number {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 8px;
      }
      
      .stat-label {
        font-size: 14px;
        color: #909399;
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
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;
          
          .title-text {
            font-size: 16px;
            font-weight: 600;
          }
          
          .mobile-price {
            font-size: 18px;
            font-weight: 700;
          }
        }
        
        .mobile-type {
          .type-tag {
            font-size: 12px;
            background-color: rgba(255, 255, 255, 0.2);
            padding: 2px 8px;
            border-radius: 4px;
          }
        }
      }
      
      .mobile-content {
        padding: 16px;
        
        .mobile-info {
          margin-bottom: 16px;
          
          .info-item {
            display: flex;
            align-items: center;
            margin-bottom: 8px;
            
            .info-icon {
              margin-right: 8px;
              font-size: 16px;
            }
            
            .info-text {
              font-size: 14px;
              color: #646566;
            }
          }
        }
        
        .mobile-description {
          .description-title {
            display: flex;
            align-items: center;
            font-size: 14px;
            font-weight: 500;
            color: #323233;
            margin-bottom: 12px;
            
            .desc-icon {
              margin-right: 6px;
              font-size: 16px;
            }
          }
          
          .description-text {
            font-size: 14px;
            line-height: 1.6;
            color: #646566;
          }
        }
      }
    }
  }
}

:deep(.el-descriptions__label) {
  font-weight: 500;
}
</style>
