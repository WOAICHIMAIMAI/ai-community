<template>
  <div class="problem-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      @submit.prevent="handleSubmit"
    >
      <el-form-item label="问题标题" prop="problem">
        <el-input
          v-model="form.problem"
          placeholder="请输入问题标题"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="问题分类" prop="type">
        <el-select
          v-model="form.type"
          placeholder="请选择问题分类"
          style="width: 100%"
        >
          <el-option
            v-for="category in categories"
            :key="category.type"
            :label="category.categoryName"
            :value="category.type"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="优先级" prop="priority">
        <el-radio-group v-model="form.priority">
          <el-radio :label="0">普通问题</el-radio>
          <el-radio :label="1">置顶问题</el-radio>
        </el-radio-group>
        <div class="form-tip">
          置顶问题会在列表中优先显示
        </div>
      </el-form-item>
      
      <el-form-item label="问题答案" prop="answer">
        <el-input
          v-model="form.answer"
          type="textarea"
          :rows="8"
          placeholder="请输入问题的详细答案"
          maxlength="2000"
          show-word-limit
        />
        <div class="form-tip">
          支持简单的Markdown格式：**粗体**、*斜体*、换行等
        </div>
      </el-form-item>
      
      <el-form-item label="答案预览">
        <div class="answer-preview" v-html="formattedAnswer"></div>
      </el-form-item>
    </el-form>
    
    <div class="form-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        {{ isEdit ? '更新' : '创建' }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createProblem, updateProblem, type ProblemForm, type ProblemCategory } from '@/api/common-problems'

// Props
interface Props {
  formData?: Partial<ProblemForm>
  categories: ProblemCategory[]
}

const props = withDefaults(defineProps<Props>(), {
  formData: () => ({})
})

// Emits
const emit = defineEmits<{
  submit: []
  cancel: []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)

// 表单数据
const form = reactive<ProblemForm>({
  id: undefined,
  problem: '',
  answer: '',
  type: undefined as any,
  priority: 0
})

// 表单验证规则
const rules: FormRules = {
  problem: [
    { required: true, message: '请输入问题标题', trigger: 'blur' },
    { min: 5, max: 200, message: '问题标题长度应在5-200个字符之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择问题分类', trigger: 'change' }
  ],
  answer: [
    { required: true, message: '请输入问题答案', trigger: 'blur' },
    { min: 10, max: 2000, message: '问题答案长度应在10-2000个字符之间', trigger: 'blur' }
  ]
}

// 计算属性
const isEdit = computed(() => {
  return !!form.id
})

const formattedAnswer = computed(() => {
  if (!form.answer) return '<div class="empty-preview">暂无内容</div>'
  
  // 简单的Markdown格式化
  return form.answer
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
})

// 监听props变化
watch(() => props.formData, (newData) => {
  if (newData) {
    Object.assign(form, {
      id: newData.id,
      problem: newData.problem || '',
      answer: newData.answer || '',
      type: newData.type,
      priority: newData.priority || 0
    })
  }
}, { immediate: true, deep: true })

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    submitting.value = true

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    emit('submit')
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  emit('cancel')
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: undefined,
    problem: '',
    answer: '',
    type: undefined,
    priority: 0
  })
}

// 暴露方法
defineExpose({
  resetForm
})
</script>

<style scoped lang="scss">
.problem-form {
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
    line-height: 1.4;
  }
  
  .answer-preview {
    min-height: 100px;
    padding: 12px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background-color: #fafafa;
    line-height: 1.6;
    
    .empty-preview {
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
  
  .form-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
  }
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-textarea__inner) {
  font-family: inherit;
}
</style>
