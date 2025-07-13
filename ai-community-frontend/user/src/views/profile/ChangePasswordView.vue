<template>
  <div class="change-password">
    <van-form @submit="onSubmit" ref="formRef">
      <van-cell-group inset>
        <van-field
          v-model="form.oldPassword"
          type="password"
          name="oldPassword"
          label="当前密码"
          placeholder="请输入当前密码"
          :rules="[{ required: true, message: '请输入当前密码' }]"
        />
        
        <van-field
          v-model="form.newPassword"
          type="password"
          name="newPassword"
          label="新密码"
          placeholder="请输入新密码"
          :rules="[
            { required: true, message: '请输入新密码' },
            { min: 6, max: 20, message: '密码长度为6-20个字符' },
            { validator: validateNewPassword, message: '新密码不能与当前密码相同' }
          ]"
        />
        
        <van-field
          v-model="form.confirmPassword"
          type="password"
          name="confirmPassword"
          label="确认密码"
          placeholder="请再次输入新密码"
          :rules="[
            { required: true, message: '请确认新密码' },
            { validator: validateConfirmPassword, message: '两次输入的密码不一致' }
          ]"
        />
      </van-cell-group>
      
      <div class="password-tip">
        <van-icon name="info-o" />
        <span>密码长度为6-20个字符，建议使用字母、数字和符号的组合</span>
      </div>
      
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          修改密码
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showFailToast, showConfirmDialog } from 'vant'
import { changePassword } from '@/api/user'
import { useAuthStore } from '@/store/auth'
import type { ChangePasswordParams } from '@/api/user'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

// 表单数据
const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证新密码不能与旧密码相同
const validateNewPassword = (val: string) => {
  return val !== form.oldPassword
}

// 验证确认密码
const validateConfirmPassword = (val: string) => {
  return val === form.newPassword
}

// 提交表单
const onSubmit = async () => {
  loading.value = true
  
  try {
    // 调用修改密码API
    const params: ChangePasswordParams = {
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    }
    
    const res = await changePassword(params)
    
    if (res.code === 200) {
      showSuccessToast('密码修改成功')
      
      // 提示用户重新登录
      showConfirmDialog({
        title: '密码已修改',
        message: '密码已成功修改，需要重新登录，是否立即跳转？',
      }).then(() => {
        // 登出并跳转到登录页面
        authStore.logout()
        router.replace('/login')
      }).catch(() => {
        // 用户取消，则返回上一页
        router.back()
      })
    } else {
      showFailToast(res.message || '密码修改失败')
    }
  } catch (error: any) {
    showFailToast(error.message || '修改密码失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.change-password {
  padding: 16px 0;
  
  .password-tip {
    display: flex;
    align-items: flex-start;
    margin: 16px;
    padding: 8px 12px;
    background-color: rgba(79, 192, 141, 0.1);
    border-radius: 4px;
    color: var(--text-color-light);
    font-size: 12px;
    line-height: 1.5;
    
    .van-icon {
      margin-right: 6px;
      margin-top: 2px;
      flex-shrink: 0;
    }
  }
}
</style> 