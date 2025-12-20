<template>
  <div class="service-detail">
    <!-- Loading状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="40">
        <Loading />
      </el-icon>
      <p>加载中...</p>
    </div>

    <!-- 无数据状态 -->
    <el-empty v-else-if="!service.id" description="暂无数据" />

    <!-- 有数据时显示 -->
    <template v-else>
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
              <div class="stat-number">{{ statistics.totalOrders }}</div>
              <div class="stat-label">总预约数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ statistics.completedOrders }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">{{ statistics.averageRating.toFixed(1) }}</div>
              <div class="stat-label">平均评分</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-number">¥{{ statistics.totalRevenue }}</div>
              <div class="stat-label">总收入</div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <!-- 最近预约 -->
      <div class="info-section">
        <h4>最近预约</h4>
        <el-table :data="recentOrders" size="small">
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
                <span class="info-text">评分：{{ statistics.averageRating.toFixed(1) }}分</span>
              </div>
              <div class="info-item">
                <span class="info-icon">📊</span>
                <span class="info-text">已服务：{{ statistics.completedOrders }}次</span>
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
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import {
  type AppointmentService,
  AppointmentType,
  AppointmentStatus,
  type ServiceStatistics,
  type ServiceRecentOrder,
  getServiceDetail,
  getServiceStatistics,
  getServiceRecentOrders
} from '@/api/appointment'
import { ElMessage } from 'element-plus'
import {
  getServiceTypeName,
  getServiceTypeTagType,
  getStatusName,
  getStatusTagType,
  formatDateTime,
  truncateText
} from '@/utils/appointmentHelper'

// Props
interface Props {
  serviceId?: number
}

const props = defineProps<Props>()

// 立即输出props
console.log('[ServiceDetail] Props定义:', props)
console.log('[ServiceDetail] serviceId:', props.serviceId)

// 响应式数据
const loading = ref(false)
const service = ref<Partial<AppointmentService>>({})
const statistics = ref<ServiceStatistics>({
  totalOrders: 0,
  completedOrders: 0,
  averageRating: 0,
  totalRevenue: 0,
  pendingOrders: 0,
  inProgressOrders: 0,
  cancelledOrders: 0
})
const recentOrders = ref<ServiceRecentOrder[]>([])

// 加载服务详情
const loadServiceDetail = async () => {
  if (!props.serviceId) {
    console.warn('[ServiceDetail] serviceId为空，无法加载数据')
    return
  }
  
  console.log('[ServiceDetail] 开始加载服务详情，ID:', props.serviceId)
  
  try {
    loading.value = true
    const res = await getServiceDetail(props.serviceId)
    console.log('[ServiceDetail] 获取服务详情响应:', res)
    
    if (res.code === 200) {
      service.value = res.data
      console.log('[ServiceDetail] 服务详情加载成功:', service.value)
    } else {
      console.error('[ServiceDetail] 获取服务详情失败:', res.message)
      ElMessage.error(res.message || '获取服务详情失败')
    }
  } catch (error) {
    console.error('[ServiceDetail] 获取服务详情异常:', error)
    ElMessage.error('获取服务详情失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  if (!props.serviceId) return
  
  try {
    const res = await getServiceStatistics(props.serviceId)
    if (res.code === 200) {
      statistics.value = res.data
    } else {
      ElMessage.error(res.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 加载最近预约
const loadRecentOrders = async () => {
  if (!props.serviceId) return
  
  try {
    const res = await getServiceRecentOrders(props.serviceId, 10)
    if (res.code === 200) {
      recentOrders.value = res.data
    } else {
      ElMessage.error(res.message || '获取预约记录失败')
    }
  } catch (error) {
    console.error('获取预约记录失败:', error)
    ElMessage.error('获取预约记录失败')
  }
}

// 加载所有数据
const loadData = async () => {
  await loadServiceDetail()
  await loadStatistics()
  await loadRecentOrders()
}

// 监听serviceId变化，重新加载数据
watch(() => props.serviceId, (newId, oldId) => {
  console.log('[ServiceDetail] serviceId变化:', oldId, '->', newId)
  if (newId && newId > 0) {
    loadData()
  }
}, { immediate: true })

</script>

<style scoped lang="scss">
.service-detail {
  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    color: #909399;
    
    .el-icon {
      margin-bottom: 16px;
    }
    
    p {
      margin: 0;
      font-size: 14px;
    }
  }
  
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
