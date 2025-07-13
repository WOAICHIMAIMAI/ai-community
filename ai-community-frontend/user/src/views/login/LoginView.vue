<template>
  <div class="login-container">
    <div class="login-header">
      <div class="logo-container">
        <div class="logo">
          <span class="logo-icon">社区</span>
          <span class="logo-text">百事通</span>
        </div>
      </div>
      <h2 class="welcome-text">欢迎使用社区百事通</h2>
      <p class="slogan">您的社区服务管家，随时随地为您解决问题</p>
    </div>

    <div class="login-form">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="loginForm.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名或手机号"
            :rules="[{ required: true, message: '请填写用户名或手机号' }]"
          />
          <van-field
            v-model="loginForm.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请填写密码' }]"
          />
        </van-cell-group>
        
        <!-- 滑块验证区域 -->
        <div class="captcha-container">
          <div class="captcha-title">
            <span>请完成人机验证</span>
            <span v-if="captchaVerified" class="captcha-verified-text">验证通过</span>
          </div>
          <div class="slider-captcha" v-if="!captchaVerified">
            <div class="captcha-track" ref="captchaTrack">
              <div class="slider-text">向右滑动滑块完成验证</div>
              <div class="captcha-slider" 
                ref="captchaSlider"
                :class="{ 'slider-active': sliderActive, 'slider-success': captchaVerified }"
                @touchstart="startSlide"
                @mousedown="startSlide">
                <van-icon name="arrow" />
              </div>
            </div>
          </div>
          <div v-else class="captcha-verified">
            <van-icon name="success" class="verified-icon" color="#07c160" size="16" />
            <span>验证成功</span>
            <van-button plain type="primary" size="mini" @click="resetCaptcha">重新验证</van-button>
          </div>
        </div>
        
        <div class="form-actions mt-16">
          <div class="remember-me">
            <van-checkbox v-model="rememberMe">记住我</van-checkbox>
          </div>
          <span class="text-link forget-link" @click="onForgetPassword">忘记密码？</span>
        </div>

        <div style="margin: 24px">
          <van-button round block type="primary" native-type="submit" :loading="isLoading" :disabled="!captchaVerified" class="login-btn">
            登录
          </van-button>
        </div>
        
        <div class="register-link">
          <span>没有账号？</span>
          <span class="text-link register-link-text" @click="goToRegister">立即注册</span>
        </div>
      </van-form>
    </div>

    <div class="app-features">
      <div class="feature-item">
        <van-icon name="service-o" size="24" color="#4fc08d" />
        <span>在线报修</span>
      </div>
      <div class="feature-item">
        <van-icon name="friends-o" size="24" color="#4fc08d" />
        <span>社区互动</span>
      </div>
      <div class="feature-item">
        <van-icon name="newspaper-o" size="24" color="#4fc08d" />
        <span>通知公告</span>
      </div>
      <div class="feature-item">
        <van-icon name="smile-comment-o" size="24" color="#4fc08d" />
        <span>智能咨询</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showSuccessToast, showFailToast } from 'vant'
import { login } from '@/api/auth'
import { useAuthStore } from '@/store/auth'
import type { LoginParams } from '@/api/auth'

// 路由实例
const router = useRouter()
const route = useRoute()

// 认证状态
const authStore = useAuthStore()

// 登录表单
const loginForm = reactive<LoginParams>({
  username: '',
  password: ''
})

// 记住我
const rememberMe = ref(false)

// 加载状态
const isLoading = ref(false)

// 滑块验证相关
const captchaTrack = ref<HTMLElement | null>(null)
const captchaSlider = ref<HTMLElement | null>(null)
const captchaVerified = ref(false)
const sliderActive = ref(false)
const trackWidth = ref(0)
const threshold = ref(0)

// 声明移动和结束处理函数
let handleMove: (moveEvent: MouseEvent | TouchEvent) => void
let handleEnd: () => void

// 初始化滑块验证
onMounted(() => {
  // 延迟执行以确保DOM已渲染
  setTimeout(initCaptcha, 100)
  
  // 处理窗口大小变化
  window.addEventListener('resize', initCaptcha)
  return () => {
    window.removeEventListener('resize', initCaptcha)
  }
})

// 初始化验证码
const initCaptcha = () => {
  if (captchaTrack.value) {
    trackWidth.value = captchaTrack.value.clientWidth
    // 设置阈值为轨道宽度的70%，降低一些难度
    threshold.value = trackWidth.value * 0.7
    console.log('初始化验证码', trackWidth.value, threshold.value)
  }
}

// 开始滑动
const startSlide = (e: MouseEvent | TouchEvent) => {
  e.preventDefault()
  if (captchaVerified.value) return
  
  sliderActive.value = true
  
  // 获取初始位置
  const startX = e instanceof MouseEvent ? e.clientX : e.touches[0].clientX
  const sliderElement = captchaSlider.value
  if (!sliderElement) return
  
  const initialOffset = sliderElement.offsetLeft
  
  // 移动事件
  handleMove = (moveEvent: MouseEvent | TouchEvent) => {
    if (!sliderActive.value || !sliderElement) return
    
    const currentX = moveEvent instanceof MouseEvent 
      ? moveEvent.clientX 
      : moveEvent.touches[0].clientX
    
    let newPosition = initialOffset + (currentX - startX)
    
    // 限制滑块范围
    const maxOffset = trackWidth.value - sliderElement.offsetWidth
    newPosition = Math.max(0, Math.min(newPosition, maxOffset))
    
    // 设置滑块位置
    sliderElement.style.left = newPosition + 'px'
    
    // 如果到达最右端或超过阈值，验证通过
    if (newPosition >= maxOffset - 5 || newPosition >= threshold.value) {
      verifySuccess(sliderElement, maxOffset)
    }
  }
  
  // 结束滑动事件
  handleEnd = () => {
    if (!sliderActive.value || !sliderElement || captchaVerified.value) return
    
    sliderActive.value = false
    
    // 如果滑块位置接近最右侧，也算验证成功
    const currentPosition = sliderElement.offsetLeft
    const maxOffset = trackWidth.value - sliderElement.offsetWidth
    
    if (currentPosition >= maxOffset - 5 || currentPosition >= threshold.value) {
      verifySuccess(sliderElement, maxOffset)
    } else {
      // 验证未通过，滑块回到起点
      sliderElement.style.left = '0px'
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

// 验证成功处理
const verifySuccess = (sliderElement: HTMLElement, maxOffset: number) => {
  captchaVerified.value = true
  sliderActive.value = false
  
  // 将滑块移到最右端
  sliderElement.style.left = maxOffset + 'px'
  
  showSuccessToast('验证成功')
  
  // 移除事件监听
  document.removeEventListener('mousemove', handleMove)
  document.removeEventListener('touchmove', handleMove)
  document.removeEventListener('mouseup', handleEnd)
  document.removeEventListener('touchend', handleEnd)
}

// 重置验证码
const resetCaptcha = () => {
  captchaVerified.value = false
  const sliderElement = captchaSlider.value
  if (sliderElement) {
    sliderElement.style.left = '0px'
  }
}

// 表单提交
const onSubmit = async () => {
  if (!captchaVerified.value) {
    showToast({
      message: '请完成人机验证',
      duration: 2000,
      forbidClick: true
    })
    return
  }
  
  try {
    isLoading.value = true
    
    // 调用登录API
    const res = await login(loginForm)
    
    // 添加调试信息
    console.log('登录响应:', res)
    
    if (res.code === 200 && res.data) {
      // 登录成功，保存用户信息和token
      const { token, username, nickName, role, avatarUrl } = res.data
      
      console.log('登录成功，用户信息:', { token, username, nickName, role, avatarUrl })
      
      authStore.login(token, {
        username,
        nickName,
        avatar: avatarUrl || '',
        role
      })
      
      // 检查登录状态
      console.log('登录后状态:', {
        token: authStore.token,
        userInfo: authStore.userInfo,
        isLoggedIn: authStore.isLoggedIn
      })
      
      showSuccessToast({
        message: '登录成功',
        duration: 2000,
        forbidClick: true
      })
      
      // 使用Vue Router进行跳转，更可靠
      console.log('准备跳转到首页')
      
      // 使用setTimeout确保状态已保存
      setTimeout(() => {
        // 获取重定向路径，如果没有则跳转到首页
        const redirectPath = route.query.redirect ? String(route.query.redirect) : '/'
        console.log('重定向到:', redirectPath)
        router.replace(redirectPath)
      }, 800)
    } else {
      showFailToast({
        message: res.msg || '登录失败，请检查用户名和密码',
        duration: 3000,
        forbidClick: true
      })
    }
  } catch (error: any) {
    console.error('登录过程中出错:', error)
    showFailToast({
      message: error.message || '登录失败，请稍后重试',
      duration: 3000,
      forbidClick: true
    })
  } finally {
    isLoading.value = false
  }
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}

// 忘记密码处理
const onForgetPassword = () => {
  showToast('请联系管理员重置密码')
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  padding: 20px;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  
  .login-header {
    padding: 40px 0 20px;
    text-align: center;
    
    .logo-container {
      display: flex;
      justify-content: center;
      margin-bottom: 20px;
    }
    
    .logo {
      display: flex;
      align-items: center;
      justify-content: center;
      
      .logo-icon {
        background-color: var(--primary-color);
        color: white;
        font-size: 20px;
        font-weight: bold;
        padding: 8px 12px;
        border-radius: 8px;
        margin-right: 6px;
      }
      
      .logo-text {
        color: var(--primary-color);
        font-size: 24px;
        font-weight: bold;
      }
    }
    
    .welcome-text {
      font-size: 24px;
      color: var(--text-color);
      margin-bottom: 8px;
    }
    
    .slogan {
      font-size: 14px;
      color: var(--text-color-light);
    }
  }
  
  .login-form {
    margin: 20px 0;
    
    .captcha-container {
      margin: 20px 16px 0;
      
      .captcha-title {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;
        font-size: 14px;
        color: var(--text-color);
        
        .captcha-verified-text {
          color: var(--success-color);
        }
      }
      
      .slider-captcha {
        height: 40px;
        background-color: #f7f8fa;
        border-radius: 20px;
        position: relative;
        
        .captcha-track {
          width: 100%;
          height: 100%;
          position: relative;
          overflow: hidden;
          border-radius: 20px;
          border: 1px solid var(--border-color);
        }
        
        .slider-text {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
          color: var(--text-color-light);
          user-select: none;
        }
        
        .captcha-slider {
          position: absolute;
          top: 0;
          left: 0;
          width: 40px;
          height: 40px;
          background-color: var(--primary-color);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          cursor: pointer;
          z-index: 2;
          transition: background-color 0.2s;
          
          &.slider-active {
            background-color: var(--primary-color-dark);
          }
          
          &.slider-success {
            background-color: var(--success-color);
          }
        }
      }
      
      .captcha-verified {
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: rgba(7, 193, 96, 0.05);
        border-radius: 20px;
        color: var(--success-color);
        font-size: 14px;
        
        .verified-icon {
          margin-right: 5px;
        }
        
        .van-button {
          margin-left: 10px;
        }
      }
    }
    
    .form-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 16px;
    }
    
    .register-link {
      text-align: center;
      color: var(--text-color-light);
      font-size: 14px;
      
      .van-button {
        color: var(--primary-color);
      }
    }
  }
  
  .app-features {
    margin-top: auto;
    padding: 20px 0;
    display: flex;
    justify-content: space-around;
    border-top: 1px solid var(--border-color);
    
    .feature-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .van-icon {
        margin-bottom: 5px;
      }
      
      span {
        font-size: 12px;
        color: var(--text-color-light);
      }
    }
  }
  
  /* 登录按钮样式优化 */
  .login-btn {
    height: 44px;
    font-size: 16px;
    font-weight: bold;
  }
  
  /* 文字链接样式 */
  .text-link {
    color: var(--primary-color);
    font-size: 14px;
    cursor: pointer;
  }
  
  .forget-link {
    font-size: 13px;
  }
  
  .register-link-text {
    font-weight: 500;
    margin-left: 5px;
  }
}
</style> 