<template>
  <div class="common-problems-container">
    <van-nav-bar
      title="常见问题"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <!-- 搜索框 -->
      <div class="search-section">
        <van-search
          v-model="searchKeyword"
          placeholder="搜索问题关键词"
          @search="onSearch"
          @clear="onClear"
        />
      </div>

      <!-- 问题分类 -->
      <div class="category-section">
        <div class="section-title">问题分类</div>
        <van-grid :column-num="2" :border="false" :gutter="10">
          <van-grid-item
            v-for="category in categories"
            :key="category.type"
            :text="category.categoryName"
            :badge="category.problemCount"
            @click="selectCategory(category.type)"
            class="category-item"
          >
            <template #icon>
              <van-icon :name="getCategoryIcon(category.type)" size="24" />
            </template>
          </van-grid-item>
        </van-grid>
      </div>

      <!-- 置顶问题 -->
      <div class="top-problems-section" v-if="topProblems.length > 0">
        <div class="section-title">
          <van-icon name="fire-o" color="#ff6b35" />
          <span>热门问题</span>
        </div>
        <van-cell-group inset>
          <van-cell
            v-for="problem in topProblems"
            :key="problem.id"
            :title="problem.problem"
            is-link
            @click="goToDetail(problem.id)"
          >
            <template #label>
              <van-tag type="danger" size="small">置顶</van-tag>
              <span class="category-tag">{{ problem.categoryName }}</span>
            </template>
          </van-cell>
        </van-cell-group>
      </div>

      <!-- 最近问题 -->
      <div class="recent-problems-section">
        <div class="section-title">
          <van-icon name="question-o" />
          <span>全部问题</span>
        </div>
        
        <!-- 分类筛选 -->
        <van-tabs v-model:active="activeCategory" @change="onCategoryChange" sticky>
          <van-tab title="全部" name="all"></van-tab>
          <van-tab
            v-for="category in categories"
            :key="category.type"
            :title="category.categoryName"
            :name="category.type"
          ></van-tab>
        </van-tabs>

        <!-- 问题列表 -->
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <van-cell-group inset v-if="problemList.length > 0">
            <van-cell
              v-for="problem in problemList"
              :key="problem.id"
              :title="problem.problem"
              is-link
              @click="goToDetail(problem.id)"
            >
              <template #label>
                <div class="problem-meta">
                  <van-tag v-if="problem.isTop" type="danger" size="small">置顶</van-tag>
                  <span class="category-tag">{{ problem.categoryName }}</span>
                </div>
              </template>
            </van-cell>
          </van-cell-group>
          
          <van-empty v-else-if="!loading" description="暂无问题" />
        </van-list>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getProblemCategories, getProblemsPage, type ProblemCategory, type ProblemDetail } from '@/api/common-problems'

const router = useRouter()

// 响应式数据
const searchKeyword = ref('')
const categories = ref<ProblemCategory[]>([])
const topProblems = ref<ProblemDetail[]>([])
const problemList = ref<ProblemDetail[]>([])
const activeCategory = ref<string | number>('all')
const loading = ref(false)
const finished = ref(false)
const currentPage = ref(1)
const pageSize = 10

// 页面初始化
onMounted(() => {
  loadCategories()
  loadTopProblems()
  loadProblems()
})

// 返回上一页
const onClickLeft = () => {
  router.back()
}

// 加载问题分类
const loadCategories = async () => {
  try {
    const response = await getProblemCategories()
    if (response.code === 200) {
      categories.value = response.data || []
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    showToast('加载分类失败')
  }
}

// 加载置顶问题
const loadTopProblems = async () => {
  try {
    const response = await getProblemsPage({
      onlyPriority: true,
      page: 1,
      pageSize: 5
    })
    if (response.code === 200) {
      topProblems.value = response.data?.records || []
    }
  } catch (error) {
    console.error('加载置顶问题失败:', error)
  }
}

// 加载问题列表
const loadProblems = async (reset = false) => {
  if (loading.value) return
  
  loading.value = true
  
  try {
    const params = {
      page: reset ? 1 : currentPage.value,
      pageSize,
      type: activeCategory.value === 'all' ? undefined : Number(activeCategory.value)
    }
    
    const response = await getProblemsPage(params)
    if (response.code === 200) {
      const newProblems = response.data?.records || []
      
      if (reset) {
        problemList.value = newProblems
        currentPage.value = 1
      } else {
        problemList.value.push(...newProblems)
      }
      
      currentPage.value++
      finished.value = newProblems.length < pageSize
    }
  } catch (error) {
    console.error('加载问题列表失败:', error)
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

// 下拉加载更多
const onLoad = () => {
  loadProblems()
}

// 分类切换
const onCategoryChange = () => {
  finished.value = false
  currentPage.value = 1
  loadProblems(true)
}

// 选择分类
const selectCategory = (type: number) => {
  activeCategory.value = type
  onCategoryChange()
}

// 搜索
const onSearch = () => {
  // TODO: 实现搜索功能
  showToast('搜索功能开发中')
}

// 清除搜索
const onClear = () => {
  searchKeyword.value = ''
}

// 跳转到问题详情
const goToDetail = (id: number) => {
  router.push(`/common-problems/detail/${id}`)
}

// 获取分类图标
const getCategoryIcon = (type: number) => {
  const iconMap: Record<number, string> = {
    1: 'user-o',      // 账户相关
    2: 'service-o',   // 维修服务
    3: 'friends-o',   // 社区使用
    4: 'gold-coin-o', // 支付账单
    5: 'setting-o',   // 技术支持
    6: 'question-o'   // 其他问题
  }
  return iconMap[type] || 'question-o'
}
</script>

<style scoped lang="scss">
.common-problems-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-top: 46px;
}

.content {
  padding: 16px;
}

.search-section {
  margin-bottom: 16px;
  
  .van-search {
    background-color: #fff;
    border-radius: 8px;
  }
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 12px;
  color: #323233;
  
  .van-icon {
    margin-right: 6px;
  }
}

.category-section {
  margin-bottom: 20px;
  
  .category-item {
    background-color: #fff;
    border-radius: 8px;
    
    :deep(.van-grid-item__content) {
      padding: 16px 8px;
    }
    
    :deep(.van-grid-item__text) {
      font-size: 14px;
      color: #323233;
    }
    
    :deep(.van-badge) {
      background-color: var(--van-primary-color);
    }
  }
}

.top-problems-section,
.recent-problems-section {
  margin-bottom: 20px;
}

.problem-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.category-tag {
  font-size: 12px;
  color: #969799;
  background-color: #f2f3f5;
  padding: 2px 6px;
  border-radius: 4px;
}

:deep(.van-tabs__wrap) {
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
}

:deep(.van-cell-group) {
  margin-bottom: 12px;
}

:deep(.van-cell) {
  padding: 12px 16px;
}

:deep(.van-cell__title) {
  font-size: 14px;
  line-height: 1.4;
}
</style>
