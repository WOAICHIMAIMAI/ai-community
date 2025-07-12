<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo">
          <span class="logo-icon">社区</span>
          <span class="logo-text">百事通</span>
        </div>
        <h2>管理后台</h2>
        <p>欢迎回来，请登录您的账号</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        class="login-form"
      >
        <el-form-item prop="username" label="用户名">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名或手机号"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password" label="密码">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item prop="captcha" label="人机验证" class="captcha-item">
          <div class="slider-captcha" v-if="!captchaVerified">
            <div class="captcha-track" ref="captchaTrack">
              <div class="captcha-bg">
                <div class="captcha-puzzle-piece" :style="puzzleStyle"></div>
                <div class="captcha-target" :style="targetStyle"></div>
              </div>
              <div 
                class="captcha-slider" 
                ref="captchaSlider"
                :class="{ 'slider-active': sliderActive, 'slider-success': captchaVerified }"
                @mousedown="startSlide"
                @touchstart="startSlide"
              >
                <el-icon><DArrowRight /></el-icon>
              </div>
              <div class="slider-text">
                {{ captchaVerified ? '验证通过' : '请向右滑动滑块完成验证' }}
              </div>
            </div>
          </div>
          <div v-else class="captcha-verified">
            <el-icon class="verified-icon"><Check /></el-icon>
            <span>验证通过</span>
            <el-button type="text" @click="resetCaptcha" class="reset-btn">重新验证</el-button>
          </div>
        </el-form-item>
        
        <div class="login-options">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <el-link type="primary">忘记密码？</el-link>
        </div>
        
        <el-button
          type="primary"
          :loading="loading"
          class="login-button"
          @click="handleLogin"
          :disabled="!captchaVerified"
        >登 录</el-button>
      </el-form>
      
      <div class="login-footer">
        <p>社区百事通管理系统 &copy; {{ new Date().getFullYear() }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { login } from '@/api/auth'
import { useAuthStore } from '@/store/auth'
import { Check, DArrowRight } from '@element-plus/icons-vue'

// 获取路由实例
const router = useRouter()
const route = useRoute()

// 获取认证状态管理
const authStore = useAuthStore()

// 表单引用
const loginFormRef = ref<FormInstance>()

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 记住我选项
const rememberMe = ref(false)

// 加载状态
const loading = ref(false)

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 人机验证相关
const captchaTrack = ref<HTMLElement | null>(null)
const captchaSlider = ref<HTMLElement | null>(null)
const captchaVerified = ref(false)
const sliderActive = ref(false)

// 滑块位置和目标位置
const sliderPosition = ref(0)
const targetPosition = ref(0)
const maxPosition = ref(0)

// 滑块和目标区样式
const puzzleStyle = computed(() => ({
  left: `${sliderPosition.value}px`,
  backgroundPosition: `-${sliderPosition.value}px 0`
}))

const targetStyle = computed(() => ({
  left: `${targetPosition.value}px`
}))

// 初始化验证码
onMounted(() => {
  initCaptcha()
})

// 初始化验证码
const initCaptcha = () => {
  if (captchaTrack.value) {
    const trackWidth = captchaTrack.value.clientWidth
    maxPosition.value = trackWidth - 60 // 60px 是滑块宽度
    
    // 随机生成目标位置 (40~80% 的位置)
    targetPosition.value = Math.floor(Math.random() * (maxPosition.value * 0.4) + maxPosition.value * 0.4)
  }
}

// 开始滑动
const startSlide = (e: MouseEvent | TouchEvent) => {
  e.preventDefault()
  if (captchaVerified.value) return
  
  sliderActive.value = true
  
  // 获取初始位置
  const startX = e instanceof MouseEvent ? e.clientX : e.touches[0].clientX
  const initialPosition = sliderPosition.value
  
  // 移动事件
  const handleMove = (moveEvent: MouseEvent | TouchEvent) => {
    if (!sliderActive.value) return
    
    const currentX = moveEvent instanceof MouseEvent 
      ? moveEvent.clientX 
      : moveEvent.touches[0].clientX
    
    let newPosition = initialPosition + (currentX - startX)
    
    // 限制滑块范围
    newPosition = Math.max(0, Math.min(newPosition, maxPosition.value))
    sliderPosition.value = newPosition
  }
  
  // 结束滑动事件
  const handleEnd = () => {
    if (!sliderActive.value) return
    sliderActive.value = false
    
    // 验证是否滑到正确位置 (允许10px的误差)
    if (Math.abs(sliderPosition.value - targetPosition.value) < 10) {
      captchaVerified.value = true
      sliderPosition.value = targetPosition.value
      ElMessage.success('验证通过')
    } else {
      // 验证失败，重置滑块
      sliderPosition.value = 0
      ElMessage.error('验证失败，请重试')
    }
    
    // 移除事件监听
    document.removeEventListener('mousemove', handleMove)
    document.removeEventListener('touchmove', handleMove)
    document.removeEventListener('mouseup', handleEnd)
    document.removeEventListener('touchend', handleEnd)
  }
  
  // 添加事件监听
  document.addEventListener('mousemove', handleMove)
  document.addEventListener('touchmove', handleMove)
  document.addEventListener('mouseup', handleEnd)
  document.addEventListener('touchend', handleEnd)
}

// 重置验证码
const resetCaptcha = () => {
  captchaVerified.value = false
  sliderPosition.value = 0
  initCaptcha()
}

// 处理登录
const handleLogin = () => {
  if (!loginFormRef.value) return
  if (!captchaVerified.value) {
    ElMessage.warning('请完成人机验证')
    return
  }
  
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        // 调用登录API
        const res = await login({
          username: loginForm.username,
          password: loginForm.password
        })
        
        // 处理登录成功
        if (res.code === 200 && res.data) {
          const { token, ...userInfo } = res.data
          
          // 保存认证信息
          authStore.login(token, userInfo)
          
          // 登录成功提示
          ElMessage.success('登录成功')
          
          // 重定向到之前尝试访问的页面或默认页面
          const redirectPath = route.query.redirect as string || '/'
          router.replace(redirectPath)
        } else {
          ElMessage.error(res.message || '登录失败')
          // 登录失败重置验证码
          resetCaptcha()
        }
      } catch (error: any) {
        ElMessage.error(error.message || '登录失败，请稍后再试')
        // 登录失败重置验证码
        resetCaptcha()
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #1890ff 0%, #0050b3 100%);
  
  .login-card {
    width: 420px;
    padding: 40px;
    background-color: white;
    border-radius: 12px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    
    .login-header {
      text-align: center;
      margin-bottom: 30px;
      
      .logo {
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 20px;
        
        .logo-icon {
          height: 40px;
          width: 40px;
          background-color: #1890ff;
          color: white;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: bold;
          font-size: 16px;
        }
        
        .logo-text {
          margin-left: 12px;
          color: #1890ff;
          font-size: 22px;
          font-weight: 600;
        }
      }
      
      h2 {
        font-size: 24px;
        margin-bottom: 12px;
        color: #262626;
        font-weight: 600;
      }
      
      p {
        font-size: 15px;
        color: #595959;
      }
    }
    
    .login-form {
      .el-form-item__label {
        font-weight: 500;
        color: #262626;
        padding-bottom: 8px;
      }
      
      .el-input {
        --el-input-height: 45px;
        
        :deep(.el-input__wrapper) {
          box-shadow: 0 0 0 1px #d9d9d9 inset;
          padding: 0 15px;
          transition: all 0.3s;
          
          &:hover {
            box-shadow: 0 0 0 1px #1890ff inset;
          }
          
          &.is-focus {
            box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2) inset;
          }
        }
      }
      
      .captcha-item {
        margin-bottom: 20px;
        
        .slider-captcha {
          position: relative;
          width: 100%;
          height: 40px;
          
          .captcha-track {
            position: relative;
            width: 100%;
            height: 100%;
            background-color: #f5f5f5;
            border-radius: 4px;
            overflow: hidden;
            
            .captcha-bg {
              position: relative;
              width: 100%;
              height: 100%;
              
              .captcha-puzzle-piece {
                position: absolute;
                width: 40px;
                height: 40px;
                background: rgba(24, 144, 255, 0.2);
                border: 2px solid #1890ff;
                border-radius: 2px;
                top: 0;
                transition: left 0.3s;
              }
              
              .captcha-target {
                position: absolute;
                width: 40px;
                height: 40px;
                background-color: rgba(82, 196, 26, 0.15);
                border: 2px dashed #52c41a;
                border-radius: 2px;
                top: 0;
              }
            }
            
            .captcha-slider {
              position: absolute;
              width: 50px;
              height: 40px;
              background-color: #fff;
              box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
              border-radius: 4px;
              display: flex;
              align-items: center;
              justify-content: center;
              cursor: pointer;
              color: #8c8c8c;
              transition: all 0.3s;
              left: 0;
              top: 0;
              z-index: 10;
              
              &.slider-active {
                color: #1890ff;
                box-shadow: 0 0 10px rgba(24, 144, 255, 0.3);
              }
              
              &.slider-success {
                background-color: #52c41a;
                color: #fff;
              }
            }
            
            .slider-text {
              position: absolute;
              left: 60px;
              top: 0;
              height: 40px;
              line-height: 40px;
              color: #8c8c8c;
              font-size: 13px;
            }
          }
        }
        
        .captcha-verified {
          display: flex;
          align-items: center;
          height: 40px;
          padding: 0 15px;
          background-color: #f6ffed;
          border: 1px solid #b7eb8f;
          border-radius: 4px;
          
          .verified-icon {
            color: #52c41a;
            font-size: 16px;
            margin-right: 8px;
          }
          
          span {
            color: #52c41a;
            font-size: 14px;
            flex: 1;
          }
          
          .reset-btn {
            font-size: 13px;
          }
        }
      }
      
      .login-options {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 24px;
      }
      
      .login-button {
        width: 100%;
        padding: 12px;
        font-size: 16px;
        font-weight: 500;
        height: 45px;
        border-radius: 6px;
      }
    }
    
    .login-footer {
      margin-top: 30px;
      text-align: center;
      
      p {
        font-size: 13px;
        color: #8c8c8c;
      }
    }
  }
}

// 响应式适配
@media screen and (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 30px 20px;
  }
}
</style> 