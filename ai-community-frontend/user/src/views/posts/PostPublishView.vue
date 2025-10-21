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
            :after-read="afterRead"
          />
        </div>

        <!-- 提交按钮 -->
        <div style="margin: 16px;">
          <van-button 
            round 
            block 
            type="primary" 
            native-type="submit" 
            :loading="submitting && !savingDraft"
            style="margin-bottom: 12px;"
          >
            {{ submitting && !savingDraft ? '发布中...' : '发布帖子' }}
          </van-button>
          <van-button 
            round 
            block 
            plain 
            type="primary" 
            @click="saveDraft"
            :loading="savingDraft"
          >
            {{ savingDraft ? '保存中...' : '保存草稿' }}
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, showFailToast, showLoadingToast } from 'vant'
import { createPost, getCategories } from '@/api/post'
import { uploadFile } from '@/api/common'

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
const categories = ref<Array<{ text: string; value: string }>>([])

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategories()
    if (res.code === 200 && res.data) {
      categories.value = res.data.map((name: string) => ({
        text: name,
        value: name
      }))
    } else {
      // 如果接口失败，使用默认分类
      categories.value = [
        { text: '公告', value: '公告' },
        { text: '求助', value: '求助' },
        { text: '分享', value: '分享' },
        { text: '讨论', value: '讨论' },
        { text: '动态', value: '动态' },
        { text: '闲置', value: '闲置' }
      ]
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    // 使用默认分类
    categories.value = [
      { text: '公告', value: '公告' },
      { text: '求助', value: '求助' },
      { text: '分享', value: '分享' },
      { text: '讨论', value: '讨论' },
      { text: '动态', value: '动态' },
      { text: '闲置', value: '闲置' }
    ]
  }
}

// 页面加载时获取分类
onMounted(() => {
  loadCategories()
})

// 提交状态
const submitting = ref(false)
const savingDraft = ref(false)

// 分类选择确认
const onCategoryConfirm = ({ selectedOptions }: any) => {
  formData.category = selectedOptions[0]?.value || selectedOptions[0]
  showCategoryPicker.value = false
}

// 表单提交
const onSubmit = async (values?: any) => {
  try {
    submitting.value = true
    
    // 处理图片URL
    const imageUrls = formData.images
      .filter((item) => item.url || item.content)
      .map((item) => {
        if (typeof item === 'string') return item
        return item.url || item.content
      })

    const postData = {
      title: formData.title,
      content: formData.content,
      category: formData.category,
      images: imageUrls.length > 0 ? imageUrls : undefined,
      status: 1 // 1表示已发布
    }

    // 调用发布接口
    const res = await createPost(postData)

    if (res.code === 200) {
      showSuccessToast('发布成功')
      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.replace('/my-posts')
      }, 1000)
    } else {
      showFailToast(res.msg || '发布失败，请重试')
    }
  } catch (error: any) {
    console.error('发布帖子失败:', error)
    showFailToast(error.message || '发布失败，请重试')
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

// 图片上传后处理
const afterRead = async (file: any) => {
  console.log('afterRead 被调用，文件:', file)
  
  const toast = showLoadingToast({
    message: '上传中...',
    forbidClick: true,
    duration: 0
  })

  try {
    // 处理多张图片上传
    const files = Array.isArray(file) ? file : [file]
    
    for (const item of files) {
      if (item.file) {
        console.log('开始上传文件:', item.file.name)
        item.status = 'uploading'
        item.message = '上传中...'
        
        const res = await uploadFile(item.file)
        console.log('上传响应:', res)
        
        if (res.code === 200 && res.data) {
          // 将上传成功的URL保存到file对象中
          item.url = res.data
          item.content = res.data
          item.status = 'done'
          item.message = ''
          console.log('图片上传成功，URL:', res.data)
        } else {
          item.status = 'failed'
          item.message = '上传失败'
          showFailToast(res.msg || '图片上传失败')
          console.error('上传失败:', res)
          // 移除上传失败的图片
          const index = formData.images.indexOf(item)
          if (index > -1) {
            formData.images.splice(index, 1)
          }
        }
      }
    }
    toast.close()
    showSuccessToast('上传成功')
  } catch (error) {
    console.error('图片上传失败:', error)
    toast.close()
    showFailToast('图片上传失败')
    
    // 移除上传失败的图片
    const files = Array.isArray(file) ? file : [file]
    for (const item of files) {
      const index = formData.images.indexOf(item)
      if (index > -1) {
        formData.images.splice(index, 1)
      }
    }
  }
}

// 保存草稿
const saveDraft = async () => {
  // 验证必填字段
  if (!formData.title || formData.title.trim().length < 5) {
    showToast('标题至少需要5个字')
    return
  }
  
  if (!formData.content || formData.content.trim().length < 10) {
    showToast('内容至少需要10个字')
    return
  }

  try {
    savingDraft.value = true
    
    // 处理图片URL
    const imageUrls = formData.images
      .filter((item) => item.url || item.content)
      .map((item) => {
        if (typeof item === 'string') return item
        return item.url || item.content
      })

    const postData = {
      title: formData.title,
      content: formData.content,
      category: formData.category || '生活', // 草稿可以没有分类，给个默认值
      images: imageUrls.length > 0 ? imageUrls : undefined,
      status: 0 // 0表示草稿
    }

    // 调用发布接口
    const res = await createPost(postData)

    if (res.code === 200) {
      showSuccessToast('草稿保存成功')
      // 延迟跳转
      setTimeout(() => {
        router.replace('/posts/my')
      }, 1000)
    } else {
      showFailToast(res.msg || '保存失败，请重试')
    }
  } catch (error: any) {
    console.error('保存草稿失败:', error)
    showFailToast(error.message || '保存失败，请重试')
  } finally {
    savingDraft.value = false
  }
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