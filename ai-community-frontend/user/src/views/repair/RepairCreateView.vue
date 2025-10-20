<template>
  <div class="repair-create-container">
    <van-nav-bar
      title="创建报修"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <van-form @submit="onSubmit">
        <!-- 报修类型 -->
        <van-cell-group inset>
          <van-field
            v-model="formData.repairType"
            is-link
            readonly
            name="repairType"
            label="报修类型"
            placeholder="请选择报修类型"
            @click="showTypePicker = true"
            :rules="[{ required: true, message: '请选择报修类型' }]"
          />
          <van-popup v-model:show="showTypePicker" position="bottom">
            <van-picker
              :columns="repairTypes"
              @confirm="onTypeConfirm"
              @cancel="showTypePicker = false"
              :default-index="0"
            />
          </van-popup>

          <!-- 报修标题 -->
          <van-field
            v-model="formData.title"
            name="title"
            label="报修标题"
            placeholder="请简要描述问题，如：水龙头漏水"
            :rules="[
              { required: true, message: '请填写报修标题' }
            ]"
          />

          <!-- 详细描述 -->
          <van-field
            v-model="formData.description"
            rows="3"
            autosize
            type="textarea"
            name="description"
            label="详细描述"
            placeholder="请详细描述故障情况"
            :rules="[
              { required: true, message: '请填写详细描述' }
            ]"
          />

          <!-- 报修地址 -->
          <van-field
            v-model="formData.address"
            is-link
            readonly
            name="address"
            label="报修地址"
            placeholder="请选择报修地址"
            @click="goToAddressBook"
            :rules="[{ required: true, message: '请选择报修地址' }]"
          />

          <!-- 联系电话 -->
          <van-field
            v-model="formData.contactPhone"
            name="contactPhone"
            label="联系电话"
            placeholder="请输入联系电话"
            :rules="[
              { required: true, message: '请填写联系电话' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
            ]"
          />

          <!-- 期望上门时间 -->
          <van-field
            v-model="formData.expectedTime"
            is-link
            readonly
            name="expectedTime"
            label="期望上门"
            placeholder="请选择期望上门时间"
            @click="showDatetimePicker = true"
          />
          <van-popup v-model:show="showDatetimePicker" position="bottom">
            <van-picker
              title="选择日期和时间"
              :columns="datetimeColumns"
              @confirm="onDatetimeConfirm"
              @cancel="showDatetimePicker = false"
            />
          </van-popup>
        </van-cell-group>

        <!-- 现场照片 -->
        <div class="upload-section">
          <div class="section-title">现场照片（选填，最多3张）</div>
          <van-uploader
            v-model="imageFiles"
            multiple
            :max-count="3"
            :max-size="5 * 1024 * 1024"
            @oversize="onOversize"
            :after-read="afterRead"
            @delete="onDeleteImage"
          />
        </div>

        <!-- 提交按钮 -->
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit" :loading="submitting">
            {{ submitting ? '提交中...' : '提交报修' }}
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onActivated, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, showFailToast } from 'vant'
import { createRepairOrder, getRepairTypes } from '@/api/repair'
import { useAuthStore } from '@/store/auth'
import { getAddressBookList, type AddressBook } from '@/api/addressBook'
import { uploadFile } from '@/api/common'

const router = useRouter()
const authStore = useAuthStore()

// 表单数据
const formData = reactive({
  addressId: 0,
  address: '',
  repairType: '',
  title: '',
  description: '',
  images: '',
  contactPhone: '',
  expectedTime: ''
})

// 图片上传
const imageFiles = ref<any[]>([])

// 选择器
const showTypePicker = ref(false)
const showDatetimePicker = ref(false)

// 报修类型 - 将字符串数组转换为Picker组件需要的对象数组格式
const repairTypes = getRepairTypes().map(type => ({
  text: type,
  value: type
}))

// 日期选择器范围
const minDate = new Date()
const maxDate = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) // 30天后

// 日期选择器列 - 生成未来30天的日期列表
const datetimeColumns = computed(() => {
  const dateColumn = [];
  const now = new Date();
  
  // 生成未来30天的日期
  for (let i = 0; i < 30; i++) {
    const targetDate = new Date(now.getTime() + i * 24 * 60 * 60 * 1000);
    
    const year = targetDate.getFullYear();
    const month = String(targetDate.getMonth() + 1).padStart(2, '0');
    const day = String(targetDate.getDate()).padStart(2, '0');
    const dayOfWeek = ['日', '一', '二', '三', '四', '五', '六'][targetDate.getDay()];
    
    let datePrefix = '';
    if (i === 0) {
      datePrefix = '今天';
    } else if (i === 1) {
      datePrefix = '明天';
    } else if (i === 2) {
      datePrefix = '后天';
    } else {
      datePrefix = `${month}月${day}日`;
    }
    
    dateColumn.push({
      text: `${datePrefix} (周${dayOfWeek})`,
      value: `${year}-${month}-${day}`
    });
  }
  
  return [dateColumn];
});

// 提交状态
const submitting = ref(false)

// 加载默认地址
const loadDefaultAddress = async () => {
  try {
    const response = await getAddressBookList()
    if (response.code === 200 && response.data && response.data.length > 0) {
      // 查找默认地址
      const defaultAddress = response.data.find((addr: AddressBook) => addr.isDefault === 1)
      
      if (defaultAddress) {
        formData.addressId = defaultAddress.id
        formData.address = `${defaultAddress.province}${defaultAddress.city}${defaultAddress.district}${defaultAddress.detail}`
        
        // 如果默认地址有联系电话，且表单中联系电话为空，则使用默认地址的电话
        if (defaultAddress.phone && !formData.contactPhone) {
          formData.contactPhone = defaultAddress.phone
        }
      }
    }
  } catch (error) {
    console.error('加载默认地址失败:', error)
    // 不显示错误提示，静默失败
  }
}

// 初始化
onMounted(async () => {
  // 检查登录状态
  if (!authStore.isLoggedIn) {
    showToast('请先登录')
    router.push('/login')
    return
  }
  
  // 先尝试恢复保存的表单数据
  restoreFormData()
  
  // 如果没有恢复的表单数据，则初始化
  if (!formData.contactPhone && authStore.userInfo?.phone) {
    formData.contactPhone = authStore.userInfo.phone
  }
  
  // 如果没有地址，加载默认地址
  if (!formData.address) {
    await loadDefaultAddress()
  }
  
  // 监听从地址簿返回后的地址选择
  checkSelectedAddress()
})

// 跳转到地址簿选择地址
const goToAddressBook = () => {
  // 保存当前表单数据
  saveFormData()
  router.push({ 
    path: '/address-book', 
    query: { select: 'true' } 
  })
}

// 保存表单数据到 sessionStorage
const saveFormData = () => {
  window.sessionStorage.setItem('repairFormData', JSON.stringify({
    ...formData,
    imageFiles: imageFiles.value
  }))
}

// 恢复表单数据
const restoreFormData = () => {
  const savedDataStr = window.sessionStorage.getItem('repairFormData')
  if (savedDataStr) {
    try {
      const savedData = JSON.parse(savedDataStr)
      Object.assign(formData, savedData)
      if (savedData.imageFiles) {
        imageFiles.value = savedData.imageFiles
      }
    } catch (error) {
      console.error('恢复表单数据失败:', error)
    }
  }
}

// 清除保存的表单数据
const clearFormData = () => {
  window.sessionStorage.removeItem('repairFormData')
}

// 检查是否从地址簿选择了地址
const checkSelectedAddress = () => {
  const selectedAddressStr = window.sessionStorage.getItem('selectedAddress')
  if (selectedAddressStr) {
    try {
      const selectedAddress = JSON.parse(selectedAddressStr)
      formData.addressId = selectedAddress.id
      // 格式化完整地址：省 + 市 + 区 + 详细地址
      formData.address = `${selectedAddress.province}${selectedAddress.city}${selectedAddress.district}${selectedAddress.detail}`
      // 清除缓存
      window.sessionStorage.removeItem('selectedAddress')
    } catch (error) {
      console.error('解析地址信息失败:', error)
    }
  }
}

// 监听页面显示事件，用于从地址簿返回时更新地址
onActivated(() => {
  // 恢复表单数据
  restoreFormData()
  // 检查是否选择了新地址
  checkSelectedAddress()
})

// 报修类型选择确认
const onTypeConfirm = ({ selectedOptions }: any) => {
  formData.repairType = selectedOptions[0]?.text || ''
  showTypePicker.value = false
}

// 日期选择确认
const onDatetimeConfirm = ({ selectedOptions }: any) => {
  if (selectedOptions && selectedOptions.length === 1) {
    formData.expectedTime = selectedOptions[0].value
  }
  showDatetimePicker.value = false
}

// 图片大小限制
const onOversize = () => {
  showToast('图片大小不能超过5M')
}

// 图片上传后处理
const afterRead = async (file: any) => {
  const validTypes = ['image/jpeg', 'image/png', 'image/gif']
  
  // 处理单个文件上传
  const handleUpload = async (fileItem: any) => {
    // 验证文件类型
    if (!validTypes.includes(fileItem.file.type)) {
      showToast('请上传jpg、png、gif格式图片')
      // 从列表中移除
      const index = imageFiles.value.indexOf(fileItem)
      if (index > -1) {
        imageFiles.value.splice(index, 1)
      }
      return
    }
    
    // 设置上传状态
    fileItem.status = 'uploading'
    fileItem.message = '上传中...'
    
    try {
      // 调用上传接口
      const res: any = await uploadFile(fileItem.file)
      if (res && res.code === 200 && res.data) {
        // 上传成功，设置服务器返回的URL
        fileItem.status = 'done'
        fileItem.message = ''
        fileItem.url = res.data
        fileItem.content = res.data
      } else {
        // 上传失败
        fileItem.status = 'failed'
        fileItem.message = '上传失败'
        showFailToast(res?.message || '图片上传失败')
      }
    } catch (error) {
      console.error('图片上传失败:', error)
      fileItem.status = 'failed'
      fileItem.message = '上传失败'
      showFailToast('图片上传失败，请重试')
    }
  }
  
  // 如果是数组，上传所有文件
  if (Array.isArray(file)) {
    await Promise.all(file.map(handleUpload))
  } else {
    await handleUpload(file)
  }
}

// 删除图片
const onDeleteImage = () => {
  // 更新images字段
  updateImagesField()
}

// 更新images字段
const updateImagesField = () => {
  // 从已上传的图片中提取URL
  const imageUrls = imageFiles.value.map((item: any) => {
    // 优先使用url字段（上传成功后的服务器URL）
    if (item.url) return item.url
    if (item.content) return item.content
    return ''
  }).filter(url => url !== '')
  
  formData.images = imageUrls.join(',')
}

// 表单提交
const onSubmit = async () => {
  // 先处理图片
  updateImagesField()
  
  try {
    submitting.value = true
    
    const res: any = await createRepairOrder(formData)
    
    if (res && res.code === 200) {
      showSuccessToast('报修提交成功')
      
      // 提交成功后清除保存的表单数据
      clearFormData()
      
      // 跳转到报修详情页
      if (res.data) {
        router.replace(`/repair/detail/${res.data}`)
      } else {
        router.replace('/repair/list')
      }
    } else {
      showFailToast(res?.message || '提交失败，请重试')
    }
  } catch (error) {
    console.error('提交报修失败:', error)
    showFailToast('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 返回
const onClickLeft = () => {
  // 返回时清除保存的表单数据
  clearFormData()
  router.replace('/repair')
}
</script>

<style scoped lang="scss">
.repair-create-container {
  min-height: 100vh;
  padding-top: 46px;
  background-color: #f7f8fa;
}

.content {
  padding: 16px 0;
}

.upload-section {
  background-color: #fff;
  margin: 10px 16px;
  padding: 16px;
  border-radius: 8px;
  
  .section-title {
    margin-bottom: 16px;
    font-size: 14px;
    color: #646566;
  }
}
</style> 