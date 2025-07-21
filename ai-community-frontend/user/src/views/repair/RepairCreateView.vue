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
              { required: true, message: '请填写报修标题' },
              { min: 3, max: 20, message: '标题长度应在3-20字之间' }
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
              { required: true, message: '请填写详细描述' },
              { min: 5, max: 200, message: '描述长度应在5-200字之间' }
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
            @click="showAddressPicker = true"
            :rules="[{ required: true, message: '请选择报修地址' }]"
          />
          <van-popup v-model:show="showAddressPicker" position="bottom">
            <van-picker
              :columns="addresses"
              @confirm="onAddressConfirm"
              @cancel="showAddressPicker = false"
              :default-index="0"
            />
          </van-popup>

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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, showFailToast } from 'vant'
import { createRepairOrder, getRepairTypes } from '@/api/repair'
import { useAuthStore } from '@/store/auth'

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
const showAddressPicker = ref(false)
const showDatetimePicker = ref(false)

// 报修类型
const repairTypes = getRepairTypes()

// 日期选择器范围
const minDate = new Date()
const maxDate = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) // 30天后

// 日期时间选择器列
const datetimeColumns = computed(() => {
  // 生成日期列
  const dateColumn = [];
  const now = new Date();
  
  for (let i = 0; i < 30; i++) {
    const date = new Date(now.getTime() + i * 24 * 60 * 60 * 1000);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const dayOfWeek = ['日', '一', '二', '三', '四', '五', '六'][date.getDay()];
    
    dateColumn.push({
      text: `${month}月${day}日 周${dayOfWeek}`,
      value: `${year}-${month}-${day}`
    });
  }
  
  // 生成时间列
  const timeColumn = [];
  for (let hour = 8; hour <= 20; hour++) {
    for (let minute = 0; minute < 60; minute += 30) {
      const hourStr = String(hour).padStart(2, '0');
      const minuteStr = String(minute).padStart(2, '0');
      timeColumn.push({
        text: `${hourStr}:${minuteStr}`,
        value: `${hourStr}:${minuteStr}:00`
      });
    }
  }
  
  return [
    { values: dateColumn, defaultIndex: 0 },
    { values: timeColumn, defaultIndex: 0 }
  ];
});

// 提交状态
const submitting = ref(false)

// 模拟地址数据 - 实际应该从API获取
const addresses = [
  { text: '1号楼1单元101室', value: 1 },
  { text: '1号楼1单元102室', value: 2 },
  { text: '1号楼1单元201室', value: 3 },
  { text: '1号楼1单元202室', value: 4 },
  { text: '2号楼1单元101室', value: 5 },
  { text: '2号楼1单元102室', value: 6 }
]

// 初始化
onMounted(() => {
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
})

// 报修类型选择确认
const onTypeConfirm = (value: string) => {
  formData.repairType = value
  showTypePicker.value = false
}

// 地址选择确认
const onAddressConfirm = (value: { text: string, value: number }) => {
  formData.addressId = value.value
  formData.addressDetail = value.text
  showAddressPicker.value = false
}

// 日期时间选择确认
const onDatetimeConfirm = (values: any[]) => {
  const [dateValue, timeValue] = values;
  formData.expectedTime = `${dateValue.value} ${timeValue.value}`;
  showDatetimePicker.value = false;
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
    
    const res = await createRepairOrder(formData)
    
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