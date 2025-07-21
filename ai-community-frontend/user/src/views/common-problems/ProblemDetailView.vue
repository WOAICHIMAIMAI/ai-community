<template>
  <div class="problem-detail-container">
    <van-nav-bar
      title="问题详情"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <template v-if="loading">
        <van-skeleton title :row="10" />
      </template>
      
      <template v-else-if="problem">
        <!-- 问题卡片 -->
        <div class="problem-card">
          <div class="problem-header">
            <div class="problem-title">
              <van-tag v-if="problem.isTop" type="danger" size="small">置顶</van-tag>
              <h2>{{ problem.problem }}</h2>
            </div>
            <div class="problem-meta">
              <van-tag type="primary" size="small">{{ problem.categoryName }}</van-tag>
            </div>
          </div>
          
          <div class="problem-content">
            <div class="answer-title">
              <van-icon name="bulb-o" color="#4fc08d" />
              <span>解答</span>
            </div>
            <div class="answer-content" v-html="formatAnswer(problem.answer)"></div>
          </div>
        </div>

        <!-- 相关问题推荐 -->
        <div class="related-section" v-if="relatedProblems.length > 0">
          <div class="section-title">
            <van-icon name="question-o" />
            <span>相关问题</span>
          </div>
          <van-cell-group inset>
            <van-cell
              v-for="relatedProblem in relatedProblems"
              :key="relatedProblem.id"
              :title="relatedProblem.problem"
              is-link
              @click="goToRelatedProblem(relatedProblem.id)"
            >
              <template #label>
                <span class="category-tag">{{ relatedProblem.categoryName }}</span>
              </template>
            </van-cell>
          </van-cell-group>
        </div>

        <!-- 反馈区域 -->
        <div class="feedback-section">
          <div class="section-title">
            <van-icon name="like-o" />
            <span>这个回答对您有帮助吗？</span>
          </div>
          <div class="feedback-buttons">
            <van-button
              type="primary"
              size="small"
              round
              @click="submitFeedback(true)"
              :disabled="feedbackSubmitted"
            >
              <van-icon name="good-job-o" />
              有帮助
            </van-button>
            <van-button
              size="small"
              round
              @click="submitFeedback(false)"
              :disabled="feedbackSubmitted"
            >
              <van-icon name="delete-o" />
              没帮助
            </van-button>
          </div>
          <div v-if="feedbackSubmitted" class="feedback-thanks">
            <van-icon name="success" color="#4fc08d" />
            <span>感谢您的反馈！</span>
          </div>
        </div>

        <!-- 联系客服 -->
        <div class="contact-section">
          <van-button
            block
            round
            type="default"
            @click="contactService"
          >
            <van-icon name="service-o" />
            问题未解决？联系客服
          </van-button>
        </div>
      </template>
      
      <template v-else>
        <div class="empty-data">
          <van-empty image="error" description="问题不存在或已被删除" />
          <van-button type="primary" size="small" @click="goBack">返回列表</van-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { getProblemDetail, getProblemsPage, type ProblemDetail } from '@/api/common-problems'

const router = useRouter()
const route = useRoute()

// 响应式数据
const loading = ref(true)
const problem = ref<ProblemDetail | null>(null)
const relatedProblems = ref<ProblemDetail[]>([])
const feedbackSubmitted = ref(false)

// 页面初始化
onMounted(() => {
  const problemId = Number(route.params.id)
  if (problemId) {
    loadProblemDetail(problemId)
  } else {
    showToast('问题ID无效')
    router.back()
  }
})

// 返回上一页
const onClickLeft = () => {
  router.back()
}

// 返回列表
const goBack = () => {
  router.replace('/common-problems')
}

// 加载问题详情
const loadProblemDetail = async (id: number) => {
  try {
    loading.value = true
    const response = await getProblemDetail(id)
    
    if (response.code === 200 && response.data) {
      problem.value = response.data
      // 加载相关问题
      loadRelatedProblems(response.data.type, id)
    } else {
      showToast('问题不存在')
      router.back()
    }
  } catch (error) {
    console.error('加载问题详情失败:', error)
    showToast('加载失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 加载相关问题
const loadRelatedProblems = async (type: number, excludeId: number) => {
  try {
    const response = await getProblemsPage({
      type,
      page: 1,
      pageSize: 5
    })
    
    if (response.code === 200) {
      // 排除当前问题
      relatedProblems.value = (response.data?.records || [])
        .filter(p => p.id !== excludeId)
        .slice(0, 3)
    }
  } catch (error) {
    console.error('加载相关问题失败:', error)
  }
}

// 跳转到相关问题
const goToRelatedProblem = (id: number) => {
  router.push(`/common-problems/detail/${id}`)
}

// 格式化答案内容
const formatAnswer = (answer: string) => {
  if (!answer) return ''
  
  // 简单的格式化：将换行符转换为<br>
  return answer
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') // 粗体
    .replace(/\*(.*?)\*/g, '<em>$1</em>') // 斜体
}

// 提交反馈
const submitFeedback = (helpful: boolean) => {
  // TODO: 调用反馈API
  feedbackSubmitted.value = true
  showSuccessToast(helpful ? '感谢您的反馈！' : '我们会继续改进')
}

// 联系客服
const contactService = () => {
  // TODO: 实现联系客服功能
  showToast('客服功能开发中')
}
</script>

<style scoped lang="scss">
.problem-detail-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-top: 46px;
}

.content {
  padding: 16px;
}

.problem-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.problem-header {
  margin-bottom: 20px;
  
  .problem-title {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    margin-bottom: 8px;
    
    h2 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #323233;
      line-height: 1.4;
      flex: 1;
    }
  }
  
  .problem-meta {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.problem-content {
  .answer-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 16px;
    font-weight: 500;
    color: #323233;
    margin-bottom: 12px;
  }
  
  .answer-content {
    font-size: 14px;
    line-height: 1.6;
    color: #646566;
    
    :deep(br) {
      margin-bottom: 8px;
    }
    
    :deep(strong) {
      color: #323233;
      font-weight: 600;
    }
    
    :deep(em) {
      color: #4fc08d;
      font-style: normal;
    }
  }
}

.related-section,
.feedback-section,
.contact-section {
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 500;
  color: #323233;
  margin-bottom: 12px;
}

.category-tag {
  font-size: 12px;
  color: #969799;
  background-color: #f2f3f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.feedback-section {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  
  .feedback-buttons {
    display: flex;
    gap: 12px;
    margin-top: 12px;
  }
  
  .feedback-thanks {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 12px;
    font-size: 14px;
    color: #4fc08d;
  }
}

.contact-section {
  .van-button {
    background-color: #fff;
    border: 1px solid #ebedf0;
    color: #646566;
    
    .van-icon {
      margin-right: 6px;
    }
  }
}

.empty-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  
  .van-button {
    margin-top: 16px;
  }
}

:deep(.van-cell-group) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.van-cell) {
  padding: 12px 16px;
}

:deep(.van-skeleton) {
  padding: 20px;
}
</style>
