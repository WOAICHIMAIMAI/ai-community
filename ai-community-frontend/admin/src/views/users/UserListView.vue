<template>
  <div class="user-list-container">
    <h2 class="page-title">用户管理</h2>
    
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="searchForm.nickName" placeholder="请输入昵称" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 180px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><refresh-right /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <div class="header-operations">
            <el-button type="success" @click="handleExport">
              <el-icon><download /></el-icon>导出Excel
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="tableLoading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
        row-key="id"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip />
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatarUrl || defaultAvatar">
              {{ row.nickname?.substr(0, 1) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatTimestamp(row.registerTime) }}
          </template>
        </el-table-column>
        <el-table-column label="最后登录时间" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatTimestamp(row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" size="small" @click="handleUpdateStatus(row.id, 1)">
              启用
            </el-button>
            <el-button v-else type="danger" size="small" @click="handleUpdateStatus(row.id, 0)">
              禁用
            </el-button>
            <el-button type="primary" size="small" @click="handleViewDetail(row.id)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageParams.pageNum"
          v-model:page-size="pageParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 用户详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="用户详情"
      size="30%"
      destroy-on-close
    >
      <div class="user-detail" v-if="currentUser">
        <div class="user-avatar">
          <el-avatar :size="80" :src="currentUser.avatarUrl || defaultAvatar">
            {{ currentUser.nickname?.substr(0, 1) }}
          </el-avatar>
          <h3>{{ currentUser.nickname }}</h3>
        </div>
        
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentUser.nickname }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser.email || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ currentUser.gender === 1 ? '男' : currentUser.gender === 2 ? '女' : '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
              {{ currentUser.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatTimestamp(currentUser.registerTime) }}</el-descriptions-item>
          <el-descriptions-item label="最后登录时间">{{ formatTimestamp(currentUser.lastLoginTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, RefreshRight, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus } from '@/api/user'
import type { UserInfo } from '@/api/user'
import * as XLSX from 'xlsx'

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 搜索表单
const searchForm = reactive({
  username: '',
  nickName: '',
  phone: '',
  status: null as number | null
})

// 表格数据
const tableData = ref<UserInfo[]>([])
const tableLoading = ref(false)

// 分页参数
const pageParams = reactive({
  pageNum: 1,
  pageSize: 10
})
const total = ref(0)

// 用户详情
const drawerVisible = ref(false)
const currentUser = ref<UserInfo | null>(null)

// 时间戳转换为时间格式
const formatTimestamp = (timestamp: number | string | null | undefined): string => {
  if (!timestamp) return '-'
  
  try {
    // 如果是字符串，尝试转换为数字
    const time = typeof timestamp === 'string' ? parseInt(timestamp) : timestamp
    
    // 检查是否为有效的时间戳
    if (isNaN(time)) return '-'
    
    const date = new Date(time)
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) return '-'
    
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (e) {
    return '-'
  }
}

// 加载用户列表
const loadUserList = async () => {
  try {
    tableLoading.value = true
    const params = {
      ...pageParams,
      ...searchForm
    }
    
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null) {
        delete params[key]
      }
    })
    
    const res = await getUserList(params)
    
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取用户列表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取用户列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageParams.pageNum = 1
  loadUserList()
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.username = ''
  searchForm.nickName = ''
  searchForm.phone = ''
  searchForm.status = null
  handleSearch()
}

// 更新用户状态
const handleUpdateStatus = (userId: string | number, status: number) => {
  const action = status === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(
    `确定要${action}该用户吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await updateUserStatus(userId, status)
      
      if (res.code === 200) {
        ElMessage.success(`${action}用户成功`)
        loadUserList()
      } else {
        ElMessage.error(res.message || `${action}用户失败`)
      }
    } catch (error: any) {
      ElMessage.error(error.message || `${action}用户失败`)
    }
  }).catch(() => {
    // 取消操作
  })
}

// 查看用户详情
const handleViewDetail = (userId: string | number) => {
  // 统一转换为字符串进行比较，避免精度问题
  const userIdStr = String(userId)
  const user = tableData.value.find(item => String(item.id) === userIdStr)
  if (user) {
    currentUser.value = user
    drawerVisible.value = true
  }
}

// 导出Excel
const handleExport = async () => {
  try {
    // 获取当前筛选条件下的所有数据（不分页）
    const params = {
      pageNum: 1,
      pageSize: 10000, // 获取大量数据
      ...searchForm
    }
    
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null) {
        delete params[key]
      }
    })
    
    const res = await getUserList(params)
    
    if (res.code === 200) {
      const users = res.data.records
      
      if (users.length === 0) {
        ElMessage.warning('暂无数据可导出')
        return
      }
      
      // 准备导出的数据
      const exportData = users.map((user, index) => ({
        '序号': index + 1,
        '用户名': user.username || '-',
        '昵称': user.nickname || '-',
        '手机号': user.phone || '-',
        '性别': user.gender === 1 ? '男' : user.gender === 2 ? '女' : '未知',
        '状态': user.status === 1 ? '启用' : '禁用',
        '注册时间': formatTimestamp(user.registerTime),
        '最后登录时间': formatTimestamp(user.lastLoginTime)
      }))
      
      // 创建工作簿
      const worksheet = XLSX.utils.json_to_sheet(exportData)
      const workbook = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(workbook, worksheet, '用户列表')
      
      // 设置列宽
      const colWidths = [
        { wch: 8 },  // 序号
        { wch: 20 }, // 用户名
        { wch: 20 }, // 昵称
        { wch: 15 }, // 手机号
        { wch: 8 },  // 性别
        { wch: 10 }, // 状态
        { wch: 20 }, // 注册时间
        { wch: 20 }  // 最后登录时间
      ]
      worksheet['!cols'] = colWidths
      
      // 生成文件名（包含当前日期时间）
      const now = new Date()
      const dateStr = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}_${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}${String(now.getSeconds()).padStart(2, '0')}`
      const fileName = `用户列表_${dateStr}.xlsx`
      
      // 导出文件
      XLSX.writeFile(workbook, fileName)
      
      ElMessage.success(`成功导出 ${users.length} 条用户数据`)
    } else {
      ElMessage.error(res.message || '获取用户数据失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '导出失败')
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pageParams.pageSize = size
  loadUserList()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  pageParams.pageNum = page
  loadUserList()
}

// 组件挂载后加载数据
onMounted(() => {
  loadUserList()
})
</script>

<style scoped lang="scss">
.user-list-container {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 500;
    color: var(--text-color);
  }
  
  .search-card {
    margin-bottom: 20px;
  }
  
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .user-detail {
    padding: 20px;
    
    .user-avatar {
      text-align: center;
      margin-bottom: 30px;
      
      h3 {
        margin-top: 10px;
        font-size: 18px;
      }
    }
  }
}
</style> 