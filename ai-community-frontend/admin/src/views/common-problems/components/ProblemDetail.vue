<template>
  <div class="problem-detail">
    <div class="detail-header">
      <div class="problem-title">
        <h3>{{ problem.problem }}</h3>
        <div class="problem-meta">
          <el-tag v-if="problem.isTop" type="danger" size="small">置顶</el-tag>
          <el-tag :type="getCategoryTagType(problem.type)">
            {{ problem.categoryName }}
          </el-tag>
        </div>
      </div>
    </div>
    
    <div class="detail-content">
      <div class="info-section">
        <h4>基本信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="问题ID">
            {{ problem.id }}
          </el-descriptions-item>
          <el-descriptions-item label="问题分类">
            <el-tag :type="getCategoryTagType(problem.type)">
              {{ problem.categoryName }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="problem.isTop ? 'danger' : 'info'">
              {{ problem.isTop ? '置顶问题' : '普通问题' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag type="success">已发布</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div class="answer-section">
        <h4>问题答案</h4>
        <div class="answer-content" v-html="formattedAnswer"></div>
      </div>
      
      <div class="preview-section">
        <h4>用户端预览</h4>
        <div class="mobile-preview">
          <div class="mobile-header">
            <div class="mobile-title">
              <span class="title-text">{{ problem.problem }}</span>
              <el-tag v-if="problem.isTop" type="danger" size="small">置顶</el-tag>
            </div>
            <div class="mobile-category">
              <span class="category-tag">{{ problem.categoryName }}</span>
            </div>
          </div>
          <div class="mobile-content">
            <div class="answer-title">
              <span class="answer-icon">💡</span>
              <span>解答</span>
            </div>
            <div class="answer-text" v-html="formattedAnswer"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ProblemDetail } from '@/api/common-problems'

// Props
interface Props {
  problem: Partial<ProblemDetail>
}

const props = defineProps<Props>()

// 计算属性
const formattedAnswer = computed(() => {
  if (!props.problem.answer) return '<div class="empty-content">暂无内容</div>'
  
  // 简单的Markdown格式化
  return props.problem.answer
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
})

// 获取分类标签类型
const getCategoryTagType = (type?: number) => {
  const typeMap: Record<number, string> = {
    1: 'primary',
    2: 'success', 
    3: 'info',
    4: 'warning',
    5: 'danger',
    6: ''
  }
  return typeMap[type || 0] || ''
}
</script>

<style scoped lang="scss">
.problem-detail {
  .detail-header {
    margin-bottom: 24px;
    
    .problem-title {
      h3 {
        margin: 0 0 12px 0;
        color: #303133;
        font-size: 20px;
        line-height: 1.4;
      }
      
      .problem-meta {
        display: flex;
        gap: 8px;
        align-items: center;
      }
    }
  }
  
  .detail-content {
    .info-section,
    .answer-section,
    .preview-section {
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
    
    .answer-content {
      padding: 16px;
      background-color: #fafafa;
      border-radius: 6px;
      border: 1px solid #ebeef5;
      line-height: 1.6;
      
      .empty-content {
        color: #c0c4cc;
        font-style: italic;
      }
      
      :deep(strong) {
        font-weight: 600;
        color: #303133;
      }
      
      :deep(em) {
        color: #409eff;
        font-style: normal;
      }
      
      :deep(br) {
        margin-bottom: 8px;
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
        
        .mobile-title {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;
          
          .title-text {
            font-size: 16px;
            font-weight: 600;
            color: #323233;
            flex: 1;
          }
        }
        
        .mobile-category {
          .category-tag {
            font-size: 12px;
            color: #969799;
            background-color: #f2f3f5;
            padding: 2px 6px;
            border-radius: 4px;
          }
        }
      }
      
      .mobile-content {
        padding: 16px;
        
        .answer-title {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 14px;
          font-weight: 500;
          color: #323233;
          margin-bottom: 12px;
          
          .answer-icon {
            font-size: 16px;
          }
        }
        
        .answer-text {
          font-size: 14px;
          line-height: 1.6;
          color: #646566;
          
          :deep(strong) {
            color: #323233;
            font-weight: 600;
          }
          
          :deep(em) {
            color: #4fc08d;
            font-style: normal;
          }
          
          :deep(br) {
            margin-bottom: 8px;
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
