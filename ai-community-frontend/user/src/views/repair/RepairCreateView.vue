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
            v-model="formData.addressDetail"
            is-link
            readonly
            name="addressDetail"
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
            :before-read="beforeRead"
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

const router = useRouter()
const authStore = useAuthStore()

// 表单数据
const formData = reactive({
  addressId: 0,
  addressDetail: '',
  repairType: '',
  title: '',
  description: '',
  images: '',
  contactPhone: '',
  expectedTime: ''
})

// 图片上传
const imageFiles = ref([])

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

// 日期时间选择器列 - 生成从当前时间起每个小时的时间列表
const datetimeColumns = computed(() => {
  const timeColumn = [];
  const now = new Date();
  const currentHour = now.getHours();
  const currentMinute = now.getMinutes();
  
  // 从下一个整点小时开始
  let startHour = currentMinute >= 50 ? currentHour + 2 : currentHour + 1;
  
  // 生成未来48小时的每个小时时间段
  for (let i = 0; i < 48; i++) {
    const targetTime = new Date(now.getTime() + (startHour - currentHour + i) * 60 * 60 * 1000);
    
    const year = targetTime.getFullYear();
    const month = String(targetTime.getMonth() + 1).padStart(2, '0');
    const day = String(targetTime.getDate()).padStart(2, '0');
    const hour = targetTime.getHours();
    const hourStr = String(hour).padStart(2, '0');
    const dayOfWeek = ['日', '一', '二', '三', '四', '五', '六'][targetTime.getDay()];
    
    // 判断是今天、明天还是具体日期
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const targetDate = new Date(targetTime);
    targetDate.setHours(0, 0, 0, 0);
    const diffDays = Math.floor((targetDate.getTime() - today.getTime()) / (24 * 60 * 60 * 1000));
    
    let datePrefix = '';
    if (diffDays === 0) {
      datePrefix = '今天';
    } else if (diffDays === 1) {
      datePrefix = '明天';
    } else {
      datePrefix = `${month}月${day}日`;
    }
    
    timeColumn.push({
      text: `${datePrefix} ${hourStr}:00 (周${dayOfWeek})`,
      value: `${year}-${month}-${day} ${hourStr}:00:00`
    });
  }
  
  return [timeColumn];
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
        formData.addressDetail = `${defaultAddress.province}${defaultAddress.city}${defaultAddress.district}${defaultAddress.detail}`
        
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
  
  // 获取用户手机号
  if (authStore.userInfo?.phone) {
    formData.contactPhone = authStore.userInfo.phone
  }
  
  // 加载默认地址
  await loadDefaultAddress()
  
  // 监听从地址簿返回后的地址选择
  checkSelectedAddress()
})

// 跳转到地址簿选择地址
const goToAddressBook = () => {
  router.push({ 
    path: '/address-book', 
    query: { select: 'true' } 
  })
}

// 检查是否从地址簿选择了地址
const checkSelectedAddress = () => {
  const selectedAddressStr = window.sessionStorage.getItem('selectedAddress')
  if (selectedAddressStr) {
    try {
      const selectedAddress = JSON.parse(selectedAddressStr)
      formData.addressId = selectedAddress.id
      // 格式化完整地址：省 + 市 + 区 + 详细地址
      formData.addressDetail = `${selectedAddress.province}${selectedAddress.city}${selectedAddress.district}${selectedAddress.detail}`
      // 清除缓存
      window.sessionStorage.removeItem('selectedAddress')
    } catch (error) {
      console.error('解析地址信息失败:', error)
    }
  }
}

// 监听页面显示事件，用于从地址簿返回时更新地址
onActivated(() => {
  checkSelectedAddress()
})

// 报修类型选择确认
const onTypeConfirm = ({ selectedOptions }: any) => {
  formData.repairType = selectedOptions[0]?.text || ''
  showTypePicker.value = false
}

// 日期时间选择确认
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

// 图片验证
const beforeRead = (file: File | File[]) => {
  const validTypes = ['image/jpeg', 'image/png', 'image/gif']
  
  // 处理单个文件
  const validateFile = (f: File) => {
    if (!validTypes.includes(f.type)) {
      showToast('请上传jpg、png、gif格式图片')
      return false
    }
    return true
  }
  
  // 如果是数组，验证所有文件
  if (Array.isArray(file)) {
    return file.every(validateFile)
  }
  
  return validateFile(file)
}

// 删除图片
const onDeleteImage = () => {
  // 更新images字段
  updateImagesField()
}

// 更新images字段
const updateImagesField = async () => {
  // 在实际项目中，应该先上传图片到服务器，然后获取URL
  // 这里模拟上传成功，直接使用本地URL
  const imageUrls = imageFiles.value.map((item: any) => {
    if (item.url) return item.url
    if (item.content) return item.content
    return 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
  })
  
  formData.images = imageUrls.join(',')
}

// 表单提交
const onSubmit = async () => {
  // 先处理图片
  await updateImagesField()
  
  try {
    submitting.value = true
    
    const res: any = await createRepairOrder(formData)
    
    if (res && res.code === 200) {
      showSuccessToast('报修提交成功')
      
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