<template>
  <div class="register-container">
    <van-nav-bar
      title="用户注册"
      left-text="返回"
      left-arrow
      @click-left="goBack"
    />

    <div class="register-form">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="registerForm.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[
              { required: true, message: '请填写用户名' },
              { pattern: /^[a-zA-Z0-9_]{3,20}$/, message: '用户名由3-20位字母、数字、下划线组成' }
            ]"
          />
          <van-field
            v-model="registerForm.phone"
            type="tel"
            name="phone"
            label="手机号"
            placeholder="请输入手机号"
            :rules="[
              { required: true, message: '请填写手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
            ]"
          />
          <van-field
            v-model="registerForm.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[
              { required: true, message: '请填写密码' },
              { min: 6, max: 20, message: '密码长度为6-20个字符' }
            ]"
          />
          <van-field
            v-model="registerForm.confirmPassword"
            type="password"
            name="confirmPassword"
            label="确认密码"
            placeholder="请再次输入密码"
            :rules="[
              { required: true, message: '请确认密码' },
              { validator: validateConfirmPassword, message: '两次输入的密码不一致' }
            ]"
          />
          <van-field
            v-model="registerForm.nickname"
            name="nickname"
            label="昵称"
            placeholder="请输入昵称（选填）"
          />
        </van-cell-group>

        <div class="agreement mt-16">
          <van-checkbox v-model="agreement" shape="square">
            我已阅读并同意 <span class="agreement-link" @click.stop="showAgreement">《用户协议》</span>
          </van-checkbox>
        </div>

        <div style="margin: 24px">
          <van-button round block type="primary" native-type="submit" :loading="isLoading">
            注册
          </van-button>
        </div>
        
        <div class="login-link">
          <span>已有账号？</span>
          <van-button type="text" @click="goToLogin">立即登录</van-button>
        </div>
      </van-form>
    </div>
    
    <!-- 用户协议弹窗 -->
    <van-popup v-model:show="showAgreementPopup" round position="bottom" :style="{ height: '70%' }">
      <div class="agreement-popup">
        <div class="agreement-header">
          <span>用户协议</span>
          <van-icon name="cross" @click="showAgreementPopup = false" />
        </div>
        <div class="agreement-content">
          <h3>社区百事通用户协议</h3>
          <p>欢迎您使用社区百事通服务！</p>
          <p>为了保障您的权益，请在使用我们的服务前仔细阅读本协议的所有内容。以下内容包括但不限于用户使用须知、隐私保护、知识产权保护等条款。</p>
          <p>一、账号注册</p>
          <p>1. 您在注册账号时，必须提供真实、准确、完整的个人资料。</p>
          <p>2. 您有责任维护账号及密码的安全，对于因您个人保管不善导致的所有活动负全部责任。</p>
          <p>二、用户行为规范</p>
          <p>1. 您承诺不发布违反法律法规、社会公德的内容。</p>
          <p>2. 您在使用过程中应尊重他人权利，不进行侵犯他人合法权益的行为。</p>
          <p>三、隐私保护</p>
          <p>1. 我们尊重并保护用户的个人隐私信息。</p>
          <p>2. 未经您的许可，我们不会向任何第三方披露您的个人信息。</p>
          <p>四、免责声明</p>
          <p>1. 对于因不可抗力、系统故障、通讯故障等原因导致的服务中断，本平台不承担责任。</p>
          <p>2. 用户因违反本协议或相关法律法规规定所引起的任何后果，本平台不承担任何责任。</p>
          <p>五、协议修改</p>
          <p>1. 我们保留随时修改本协议的权利。</p>
          <p>2. 修改后的协议将在发布后即时生效。</p>
          <p>本协议的解释权归社区百事通所有。</p>
        </div>
        <div class="agreement-footer">
          <van-button type="primary" block round @click="acceptAgreement">我已阅读并同意</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, showFailToast } from 'vant'
import { register } from '@/api/auth'
import type { RegisterParams } from '@/api/auth'

// 路由实例
const router = useRouter()

// 注册表单
const registerForm = reactive<RegisterParams>({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

// 加载状态
const isLoading = ref(false)

// 用户协议同意状态
const agreement = ref(false)
const showAgreementPopup = ref(false)

// 验证确认密码
const validateConfirmPassword = (value: string) => {
  return value === registerForm.password
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login')
}

// 显示用户协议
const showAgreement = () => {
  showAgreementPopup.value = true
}

// 同意用户协议
const acceptAgreement = () => {
  agreement.value = true
  showAgreementPopup.value = false
}

// 提交注册表单
const onSubmit = async () => {
  if (!agreement.value) {
    showToast({
      message: '请阅读并同意用户协议',
      duration: 2000,
      forbidClick: true
    })
    return
  }
  
  try {
    isLoading.value = true
    
    // 调用注册API
    const res = await register(registerForm)
    
    if (res.code === 200) {
      showSuccessToast({
        message: '注册成功',
        duration: 2000,
        forbidClick: true
      })
      
      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push('/login')
      }, 1000)
    } else {
      showFailToast({
        message: res.msg || '注册失败，请检查输入信息',
        duration: 3000,
        forbidClick: true
      })
    }
  } catch (error: any) {
    console.error('注册过程中出错:', error)
    showFailToast({
      message: error.message || '注册失败，请稍后重试',
      duration: 3000,
      forbidClick: true
    })
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-container {
  min-height: 100vh;
  background-color: #fff;
  
  .register-form {
    padding: 20px;
    
    .agreement {
      padding: 0 16px;
      
      .agreement-link {
        color: var(--primary-color);
      }
    }
    
    .login-link {
      text-align: center;
      color: var(--text-color-light);
      font-size: 14px;
      
      .van-button {
        color: var(--primary-color);
      }
    }
  }
}

/* 注册按钮样式优化 */
:deep(.van-button--primary) {
  height: 44px;
  font-size: 16px;
  font-weight: bold;
}

.agreement-popup {
  display: flex;
  flex-direction: column;
  height: 100%;
  
  .agreement-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid var(--border-color);
    
    span {
      font-size: 18px;
      font-weight: bold;
    }
  }
  
  .agreement-content {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
    
    h3 {
      text-align: center;
      margin-bottom: 15px;
    }
    
    p {
      margin-bottom: 10px;
      color: var(--text-color);
      font-size: 14px;
      line-height: 1.6;
    }
  }
  
  .agreement-footer {
    padding: 16px;
    border-top: 1px solid var(--border-color);
  }
}
</style> 