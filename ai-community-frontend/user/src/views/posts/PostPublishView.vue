<template>
  <div class="publish-container">
    <van-nav-bar
      title="发布帖子"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <van-form @submit="onSubmit">
        <!-- 标题 -->
        <van-cell-group inset>
          <van-field
            v-model="formData.title"
            name="title"
            label="标题"
            placeholder="请输入标题（5-50字）"
            :rules="[
              { required: true, message: '请填写标题' },
              { min: 5, max: 50, message: '标题长度应在5-50字之间' }
            ]"
          />

          <!-- 分类 -->
          <van-field
            v-model="formData.category"
            is-link
            readonly
            name="category"
            label="分类"
            placeholder="请选择分类"
            @click="showCategoryPicker = true"
            :rules="[{ required: true, message: '请选择分类' }]"
          />
          <van-popup v-model:show="showCategoryPicker" position="bottom">
            <van-picker
              :columns="categories"
              @confirm="onCategoryConfirm"
              @cancel="showCategoryPicker = false"
              :default-index="0"
            />
          </van-popup>

          <!-- 内容 -->
          <van-field
            v-model="formData.content"
            rows="5"
            autosize
            type="textarea"
            name="content"
            label="内容"
            placeholder="请输入内容（10-1000字）"
            :rules="[
              { required: true, message: '请填写内容' },
              { min: 10, max: 1000, message: '内容长度应在10-1000字之间' }
            ]"
          />
        </van-cell-group>

        <!-- 图片上传 -->
        <div class="upload-section">
          <div class="section-title">添加图片（选填，最多9张）</div>
          <van-uploader
            v-model="formData.images"
            multiple
            :max-count="9"
            :max-size="5 * 1024 * 1024"
            @oversize="onOversize"
            :before-read="beforeRead"
          />
        </div>

        <!-- 提交按钮 -->
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit" :loading="submitting">
            {{ submitting ? '发布中...' : '发布帖子' }}
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, showFailToast } from 'vant'
import { createPost } from '@/api/post'

const router = useRouter()

// 表单数据
const formData = reactive({
  title: '',
  category: '',
  content: '',
  images: [] as any[]
})

// 分类选择
const showCategoryPicker = ref(false)
const categories = [
  '生活', '服务', '闲置', '求助', '公告'
]

// 提交状态
const submitting = ref(false)

// 分类选择确认
const onCategoryConfirm = (value: string) => {
  formData.category = value
  showCategoryPicker.value = false
}

// 表单提交
const onSubmit = async () => {
  try {
    submitting.value = true
    
    // 处理图片上传，这里暂时模拟上传成功
    const imageUrls = formData.images.map((item) => {
      if (typeof item === 'string') return item
      return item.content || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
    })

    const postData = {
      title: formData.title,
      content: formData.content,
      category: formData.category,
      images: imageUrls
    }

    // 调用发布接口
    const res = await createPost(postData)

    if (res.code === 1) {
      showSuccessToast('发布成功')
      router.replace('/')
    } else {
      showFailToast('发布失败，请重试')
    }
  } catch (error) {
    console.error('发布帖子失败:', error)
    showFailToast('发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 图片大小限制
const onOversize = () => {
  showToast('图片大小不能超过5M')
}

// 图片验证
const beforeRead = (file: File) => {
  const validTypes = ['image/jpeg', 'image/png', 'image/gif']
  
  if (!validTypes.includes(file.type)) {
    showToast('请上传jpg、png、gif格式图片')
    return false
  }
  
  return true
}

// 返回
const onClickLeft = () => {
  router.back()
}
</script>

<style scoped>
.publish-container {
  padding-top: 46px;
  min-height: 100vh;
  background-color: #f7f8fa;
}

.content {
  padding: 15px 0;
}

.upload-section {
  background-color: #fff;
  margin: 10px 0;
  padding: 16px;
  border-radius: 8px;
}

.section-title {
  margin-bottom: 10px;
  font-size: 14px;
  color: #646566;
}

:deep(.van-field__label) {
  width: 60px;
}
</style> 