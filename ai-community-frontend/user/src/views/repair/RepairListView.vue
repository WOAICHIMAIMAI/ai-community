<template>
  <div class="repair-list-container">
    <van-nav-bar
      title="我的报修"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <div class="repair-list">
        <template v-if="loading">
          <van-skeleton title :row="3" v-for="i in 3" :key="i" />
        </template>
        
        <template v-else-if="repairOrders.length > 0">
          <div
            v-for="order in repairOrders"
            :key="order.id"
            class="repair-item"
            @click="goToDetail(order.id)"
          >
            <div class="repair-header">
              <div class="repair-title">{{ order.title }}</div>
              <van-tag :type="getStatusTagType(order.status)" round>{{ getStatusText(order.status) }}</van-tag>
            </div>
            
            <div class="repair-info">
              <div class="info-item">
                <van-icon name="clock-o" />
                <span>{{ formatDate(order.createTime) }}</span>
              </div>
              <div class="info-item">
                <van-icon name="location-o" />
                <span>{{ order.addressDetail }}</span>
              </div>
            </div>
          </div>
        </template>
        
        <template v-else>
          <div class="empty-list">
            <van-empty image="search" description="暂无报修记录" />
            <van-button type="primary" size="small" @click="goToCreate">新增报修</van-button>
          </div>
        </template>
      </div>
    </div>
    
    <!-- 悬浮按钮 -->
    <div class="float-button" @click="goToCreate">
      <van-icon name="plus" size="20" />
    </div>
  </div>
</template>

<script>
import { showToast } from 'vant'

export default {
  name: 'RepairListView',
  data() {
    return {
      loading: true,
      repairOrders: []
    }
  },
  mounted() {
    // 模拟加载数据
    setTimeout(() => {
      this.repairOrders = [
        {
          id: 1,
          title: '水龙头漏水',
          repairType: '水电维修',
          addressDetail: '1号楼1单元101室',
          status: 0,
          createTime: new Date().toISOString(),
          hasFeedback: false
        },
        {
          id: 2,
          title: '门锁损坏',
          repairType: '门窗维修',
          addressDetail: '1号楼1单元102室',
          status: 1,
          createTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
          hasFeedback: false
        }
      ]
      this.loading = false
    }, 1000)
  },
  methods: {
    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        0: '待受理',
        1: '已分配',
        2: '处理中',
        3: '已完成',
        4: '已取消'
      }
      return statusMap[status] || '未知状态'
    },
    // 获取状态标签类型
    getStatusTagType(status) {
      const typeMap = {
        0: 'primary',
        1: 'warning',
        2: 'warning',
        3: 'success',
        4: 'default'
      }
      return typeMap[status] || 'default'
    },
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      
      const date = new Date(dateString)
      const now = new Date()
      const diffMs = now.getTime() - date.getTime()
      const diffDays = Math.floor(diffMs / (24 * 60 * 60 * 1000))
      
      if (diffDays > 0) {
        return `${diffDays}天前`
      } else {
        const diffHours = Math.floor(diffMs / (60 * 60 * 1000))
        if (diffHours > 0) {
          return `${diffHours}小时前`
        } else {
          return '刚刚'
        }
      }
    },
    // 跳转到详情页
    goToDetail(orderId) {
      console.log('跳转到详情页:', orderId)
      this.$router.replace(`/repair/detail/${orderId}`)
    },
    // 跳转到创建页
    goToCreate() {
      console.log('跳转到创建页')
      this.$router.replace('/repair/create')
    },
    // 返回上一页
    onClickLeft() {
      console.log('返回上一页')
      this.$router.back()
    }
  }
}
</script>

<style scoped>
.repair-list-container {
  padding-top: 46px;
  padding-bottom: 20px;
  min-height: 100vh;
  background-color: #f7f8fa;
}

.content {
  padding: 15px;
}

.repair-item {
  margin-bottom: 12px;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.repair-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.repair-title {
  font-size: 16px;
  font-weight: bold;
  flex: 1;
}

.repair-info {
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-size: 14px;
  color: #646566;
}

.info-item .van-icon {
  margin-right: 6px;
  font-size: 14px;
}

.empty-list {
  padding: 40px 0;
  text-align: center;
}

.empty-list .van-button {
  margin-top: 16px;
}

.float-button {
  position: fixed;
  right: 16px;
  bottom: 80px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #1989fa;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  z-index: 99;
}
</style> 