<template>
  <div class="profile-edit">
    <van-form @submit="onSubmit">
      <!-- 头像上传 -->
      <div class="avatar-upload">
        <van-uploader
          v-model="fileList"
          :max-count="1"
          :after-read="afterRead"
          :before-read="beforeRead"
          upload-text="点击上传头像"
          preview-size="80px"
          :preview-image="true"
        />
        <p class="upload-tip">支持jpg、png格式，大小不超过2MB</p>
      </div>
      
      <!-- 个人信息表单 -->
      <van-cell-group inset title="基本信息">
        <van-field
          v-model="userForm.username"
          name="username"
          label="用户名"
          readonly
          disabled
        />
        <van-field
          v-model="userForm.nickname"
          name="nickname"
          label="昵称"
          placeholder="请输入昵称"
          :rules="[{ required: true, message: '请填写昵称' }]"
        />
        <van-field
          v-model="userForm.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          readonly
          disabled
          :rules="[{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误' }]"
        >
          <template #button>
            <van-button size="small" type="primary" @click="showPhoneDialog = true">更换</van-button>
          </template>
        </van-field>
        
        <!-- 性别选择 -->
        <van-field
          name="gender"
          label="性别"
          readonly
          is-link
          @click="showGenderPicker = true"
          :value="genderOptions[userForm.gender]"
        />
        
        <!-- 生日选择 -->
        <van-field
          name="birthday"
          label="生日"
          readonly
          is-link
          @click="showDatePicker = true"
          :value="formatDate(userForm.birthday)"
        />
      </van-cell-group>

      <!-- 提交按钮 -->
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          保存
        </van-button>
      </div>
    </van-form>

    <!-- 性别选择弹出层 -->
    <van-popup v-model:show="showGenderPicker" position="bottom">
      <van-picker
        title="请选择性别"
        :columns="genderOptions"
        @confirm="onGenderConfirm"
        @cancel="showGenderPicker = false"
        :default-index="userForm.gender"
        show-toolbar
      />
    </van-popup>

    <!-- 日期选择弹出层 -->
    <van-popup v-model:show="showDatePicker" position="bottom">
      <van-date-picker
        title="选择生日"
        :min-date="new Date(1950, 0, 1)"
        :max-date="new Date()"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
        :default-date="userForm.birthday ? new Date(userForm.birthday) : new Date()"
      />
    </van-popup>

    <!-- 修改手机号弹窗 -->
    <van-dialog 
      v-model:show="showPhoneDialog" 
      title="更换手机号" 
      show-cancel-button
      confirm-button-text="确认更换"
      @confirm="onPhoneChange">
      <van-form>
        <van-field
          v-model="newPhone"
          label="新手机号"
          placeholder="请输入新手机号"
          :rules="[
            { required: true, message: '请输入新手机号' },
            { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误' }
          ]"
        />
        <van-field
          v-model="smsCode"
          center
          label="验证码"
          placeholder="请输入验证码"
          :rules="[{ required: true, message: '请输入验证码' }]"
        >
          <template #button>
            <van-button size="small" type="primary" :disabled="isSending" @click="sendSms">
              {{ isSending ? `${countdown}s后重新获取` : '获取验证码' }}
            </van-button>
          </template>
        </van-field>
      </van-form>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  showLoadingToast, 
  showSuccessToast, 
  closeToast, 
  showFailToast,
  type UploaderFileListItem
} from 'vant'
import { getUserInfo, updateUserInfo } from '@/api/user'
import type { UserInfo } from '@/api/user'

const router = useRouter()

// 表单数据
const userForm = reactive<Partial<UserInfo>>({
  id: 0,
  username: '',
  nickname: '',
  phone: '',
  avatarUrl: '',
  gender: 0,
  birthday: null
})

// 头像上传相关
const fileList = ref<UploaderFileListItem[]>([])
const loading = ref(false)

// 性别选择相关
const showGenderPicker = ref(false)
const genderOptions = ['未设置', '男', '女']

// 日期选择相关
const showDatePicker = ref(false)

// 手机号修改相关
const showPhoneDialog = ref(false)
const newPhone = ref('')
const smsCode = ref('')
const isSending = ref(false)
const countdown = ref(60)

// 获取用户信息
const fetchUserInfo = async () => {
  const toast = showLoadingToast({
    message: '加载中...',
    forbidClick: true,
  })
  
  try {
    const res = await getUserInfo()
    if (res.code === 200 && res.data) {
      // 将后端返回的数据填充到表单中
      Object.assign(userForm, res.data)
      
      // 如果有头像，添加到文件列表中预览
      if (userForm.avatarUrl) {
        fileList.value = [{
          url: userForm.avatarUrl,
          isImage: true,
          name: 'avatar.jpg'
        }]
      }
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
    showFailToast('获取用户信息失败')
  } finally {
    closeToast()
  }
}

// 校验图片大小和类型
const beforeRead = (file: File) => {
  // 校验文件类型
  const isImage = ['image/jpeg', 'image/png'].includes(file.type)
  if (!isImage) {
    showFailToast('请上传 jpg 或 png 格式图片')
    return false
  }
  
  // 校验文件大小
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    showFailToast('图片大小不能超过 2MB!')
    return false
  }
  
  return true
}

// 上传头像
const afterRead = (file: UploaderFileListItem) => {
  // 实际项目中这里应该调用上传接口，这里仅做模拟
  const formData = new FormData()
  
  if (file.file) {
    formData.append('file', file.file)
    
    // 临时显示上传中状态
    file.status = 'uploading'
    file.message = '上传中...'
    
    // 模拟上传请求
    setTimeout(() => {
      // 这里通常是调用后端上传接口，获取图片URL
      // 然后设置到userForm.avatarUrl
      // 此处仅做模拟，假设获取到了头像URL
      userForm.avatarUrl = URL.createObjectURL(file.file)
      
      // 更新上传状态
      file.status = 'done'
      file.message = ''
    }, 1000)
  }
}

// 性别选择确认
const onGenderConfirm = (value: string, index: number) => {
  userForm.gender = index
  showGenderPicker.value = false
}

// 日期选择确认
const onDateConfirm = (value: Date) => {
  userForm.birthday = value.toISOString().split('T')[0]
  showDatePicker.value = false
}

// 格式化日期显示
const formatDate = (dateString: string | null) => {
  if (!dateString) return '未设置'
  return dateString
}

// 发送验证码
const sendSms = () => {
  if (!newPhone.value) {
    showFailToast('请输入手机号')
    return
  }
  
  // 验证手机号格式
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(newPhone.value)) {
    showFailToast('手机号格式错误')
    return
  }
  
  // 模拟发送验证码
  isSending.value = true
  countdown.value = 60
  
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      isSending.value = false
    }
  }, 1000)
  
  showSuccessToast('验证码发送成功')
}

// 修改手机号
const onPhoneChange = () => {
  if (!newPhone.value || !smsCode.value) {
    showFailToast('请填写完整信息')
    return false
  }
  
  // 这里应该调用修改手机号API
  // 假设修改成功
  userForm.phone = newPhone.value
  newPhone.value = ''
  smsCode.value = ''
  showSuccessToast('手机号修改成功')
}

// 提交表单
const onSubmit = async () => {
  loading.value = true
  
  try {
    // 调用更新用户信息API
    const res = await updateUserInfo(userForm)
    if (res.code === 200) {
      showSuccessToast('保存成功')
      router.back()
    } else {
      showFailToast(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败', error)
    showFailToast('保存失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped lang="scss">
.profile-edit {
  padding: 16px 0;

  .avatar-upload {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px 0;
    
    .upload-tip {
      font-size: 12px;
      color: var(--text-color-light);
      margin-top: 8px;
    }
    
    :deep(.van-uploader__upload) {
      width: 80px;
      height: 80px;
    }
  }
}
</style> 