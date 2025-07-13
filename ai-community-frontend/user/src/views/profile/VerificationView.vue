<template>
  <div class="verification-container">
    <!-- 未认证状态 -->
    <template v-if="verificationStatus === 0">
      <div class="verification-status-card">
        <van-icon name="warn-o" class="status-icon" />
        <h3>您尚未完成实名认证</h3>
        <p class="status-desc">完成实名认证后可使用更多社区功能</p>
      </div>
      
      <van-form @submit="onSubmit" ref="formRef" class="verification-form">
        <van-cell-group inset title="实名认证信息">
          <van-field
            v-model="form.realName"
            name="realName"
            label="真实姓名"
            placeholder="请输入真实姓名"
            :rules="[{ required: true, message: '请输入真实姓名' }]"
          />
          <van-field
            v-model="form.idCard"
            name="idCard"
            label="身份证号"
            placeholder="请输入身份证号"
            :rules="[
              { required: true, message: '请输入身份证号' },
              { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号' }
            ]"
          />
        </van-cell-group>
        
        <!-- 身份证照片上传 -->
        <van-cell-group inset title="证件照片" class="upload-group">
          <div class="id-card-uploads">
            <div class="upload-item">
              <h4>身份证人像面</h4>
              <van-uploader
                v-model="frontIdCard"
                :max-count="1"
                :after-read="(file) => afterReadIdCard(file, 'front')"
                :before-read="beforeReadIdCard"
              >
                <template #preview-cover="{ file }">
                  <div class="preview-cover">
                    <template v-if="file.status === 'uploading'">
                      <van-loading type="spinner" size="20" />
                      <span class="upload-status">上传中...</span>
                    </template>
                  </div>
                </template>
              </van-uploader>
              <p class="upload-tip">请上传清晰的身份证人像面照片</p>
            </div>
            
            <div class="upload-item">
              <h4>身份证国徽面</h4>
              <van-uploader
                v-model="backIdCard"
                :max-count="1"
                :after-read="(file) => afterReadIdCard(file, 'back')"
                :before-read="beforeReadIdCard"
              >
                <template #preview-cover="{ file }">
                  <div class="preview-cover">
                    <template v-if="file.status === 'uploading'">
                      <van-loading type="spinner" size="20" />
                      <span class="upload-status">上传中...</span>
                    </template>
                  </div>
                </template>
              </van-uploader>
              <p class="upload-tip">请上传清晰的身份证国徽面照片</p>
            </div>
          </div>
        </van-cell-group>

        <!-- 用户隐私声明 -->
        <div class="privacy-statement">
          <van-checkbox v-model="privacyAgreed" shape="square">
            我已阅读并同意<span class="link" @click="showPrivacyPopup = true">《用户隐私声明》</span>
          </van-checkbox>
        </div>
        
        <!-- 提交按钮 -->
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit" :loading="loading" :disabled="!isFormValid">
            提交认证
          </van-button>
        </div>
      </van-form>
    </template>
    
    <!-- 认证中状态 -->
    <template v-else-if="verificationStatus === 1">
      <div class="verification-status-card status-processing">
        <van-icon name="clock-o" class="status-icon" />
        <h3>认证审核中</h3>
        <p class="status-desc">您的实名认证信息正在审核中，请耐心等待</p>
        <p class="submit-time" v-if="verificationData.submitTime">
          提交时间：{{ formatDate(verificationData.submitTime) }}
        </p>
      </div>
    </template>
    
    <!-- 认证成功状态 -->
    <template v-else-if="verificationStatus === 2">
      <div class="verification-status-card status-success">
        <van-icon name="passed" class="status-icon" />
        <h3>认证成功</h3>
        <p class="status-desc">您已通过实名认证，可以使用全部社区功能</p>
        <div class="verification-info">
          <p><span>认证姓名：</span>{{ maskName(verificationData.realName) }}</p>
          <p><span>认证号码：</span>{{ maskIdCard(verificationData.idCardNumber) }}</p>
          <p><span>认证时间：</span>{{ formatDate(verificationData.completeTime) }}</p>
        </div>
      </div>
    </template>
    
    <!-- 认证失败状态 -->
    <template v-else-if="verificationStatus === 3">
      <div class="verification-status-card status-failed">
        <van-icon name="cross" class="status-icon" />
        <h3>认证失败</h3>
        <p class="status-desc">
          {{ verificationData.failureReason || '您的实名认证未通过，请重新提交' }}
        </p>
        <van-button round type="primary" block @click="verificationStatus = 0">
          重新认证
        </van-button>
      </div>
    </template>
    
    <!-- 隐私声明弹窗 -->
    <van-popup v-model:show="showPrivacyPopup" position="bottom" round :style="{ height: '70%' }">
      <div class="privacy-popup">
        <div class="popup-title">
          <h3>用户隐私声明</h3>
          <van-icon name="cross" @click="showPrivacyPopup = false" />
        </div>
        <div class="popup-content">
          <h4>实名认证隐私保护说明</h4>
          <p>感谢您使用社区百事通平台的实名认证服务。我们非常重视您的个人隐私和数据安全，本说明旨在向您说明我们如何收集、使用、存储和保护您的实名认证信息。</p>
          
          <h4>1. 信息收集</h4>
          <p>我们收集的实名认证信息包括但不限于：</p>
          <p>- 您的真实姓名</p>
          <p>- 您的身份证号码</p>
          <p>- 您的身份证照片（正反面）</p>
          
          <h4>2. 信息用途</h4>
          <p>我们收集的实名认证信息仅用于：</p>
          <p>- 验证您的真实身份</p>
          <p>- 提供安全、可靠的社区服务</p>
          <p>- 满足相关法律法规的要求</p>
          
          <h4>3. 信息存储与保护</h4>
          <p>- 您的实名认证信息将被安全存储在我们的服务器中</p>
          <p>- 我们采用严格的加密技术和访问控制措施保护您的信息</p>
          <p>- 只有经过授权的工作人员才能访问您的实名认证信息</p>
          
          <h4>4. 信息共享</h4>
          <p>我们不会向任何第三方出售您的实名认证信息。仅在以下情况下会共享您的信息：</p>
          <p>- 获得您的明确同意</p>
          <p>- 应法律法规要求</p>
          <p>- 应国家机关依法提出的要求</p>
          
          <h4>5. 您的权利</h4>
          <p>您有权：</p>
          <p>- 查询、更正您的实名认证信息</p>
          <p>- 撤回您的实名认证</p>
          <p>- 注销您的账户（这将同时删除您的实名认证信息）</p>
        </div>
        <div class="popup-footer">
          <van-button type="primary" block round @click="agreePrivacy">我已阅读并同意</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showLoadingToast, showSuccessToast, showFailToast, closeToast } from 'vant'
import { getVerificationInfo } from '@/api/user'
import type { VerificationInfo, UploaderFileListItem } from '@/api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const showPrivacyPopup = ref(false)
const privacyAgreed = ref(false)

// 认证状态 0-未认证 1-认证中 2-已认证 3-认证失败
const verificationStatus = ref(0)
// 认证信息
const verificationData = ref<Partial<VerificationInfo>>({})

// 表单数据
const form = reactive({
  realName: '',
  idCard: '',
  frontIdCardUrl: '',
  backIdCardUrl: '',
})

// 身份证照片
const frontIdCard = ref<UploaderFileListItem[]>([])
const backIdCard = ref<UploaderFileListItem[]>([])

// 表单是否有效
const isFormValid = computed(() => {
  return form.realName && 
         form.idCard && 
         frontIdCard.value.length > 0 && 
         backIdCard.value.length > 0 && 
         privacyAgreed.value
})

// 获取认证信息
const fetchVerificationInfo = async () => {
  const toast = showLoadingToast({
    message: '加载中...',
    forbidClick: true,
  })
  
  try {
    const res = await getVerificationInfo()
    if (res.code === 200 && res.data) {
      verificationData.value = res.data
      verificationStatus.value = res.data.verificationStatus
    }
  } catch (error) {
    console.error('获取认证信息失败', error)
    // 如果获取失败，默认显示未认证状态
    verificationStatus.value = 0
  } finally {
    closeToast()
  }
}

// 校验身份证照片
const beforeReadIdCard = (file: File) => {
  // 校验文件类型
  const isImage = ['image/jpeg', 'image/png'].includes(file.type)
  if (!isImage) {
    showFailToast('请上传jpg或png格式图片')
    return false
  }
  
  // 校验文件大小
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    showFailToast('图片大小不能超过5MB')
    return false
  }
  
  return true
}

// 上传身份证照片
const afterReadIdCard = (file: UploaderFileListItem, type: 'front' | 'back') => {
  // 实际项目中应该调用上传接口，这里仅做模拟
  file.status = 'uploading'
  file.message = '上传中...'
  
  setTimeout(() => {
    // 模拟上传成功，获取图片URL
    const fileUrl = URL.createObjectURL(file.file!)
    
    // 更新表单数据
    if (type === 'front') {
      form.frontIdCardUrl = fileUrl
    } else {
      form.backIdCardUrl = fileUrl
    }
    
    file.status = 'done'
    file.message = ''
  }, 1000)
}

// 同意隐私声明
const agreePrivacy = () => {
  privacyAgreed.value = true
  showPrivacyPopup.value = false
}

// 提交表单
const onSubmit = () => {
  if (!isFormValid.value) {
    if (!privacyAgreed.value) {
      showFailToast('请先阅读并同意《用户隐私声明》')
    }
    return
  }
  
  loading.value = true
  
  // 模拟提交认证信息
  setTimeout(() => {
    loading.value = false
    
    // 提交成功后，更新状态为认证中
    verificationStatus.value = 1
    verificationData.value.submitTime = new Date().toISOString()
    
    showSuccessToast('提交成功，请等待审核')
  }, 1500)
}

// 格式化日期
const formatDate = (dateStr?: string | null) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 姓名脱敏
const maskName = (name?: string | null) => {
  if (!name) return '-'
  if (name.length === 2) {
    return name.substring(0, 1) + '*'
  } else if (name.length > 2) {
    return name.substring(0, 1) + '*'.repeat(name.length - 2) + name.substring(name.length - 1)
  }
  return '*' + name
}

// 身份证号脱敏
const maskIdCard = (idCard?: string | null) => {
  if (!idCard) return '-'
  if (idCard.length >= 18) {
    return idCard.substring(0, 6) + '**********' + idCard.substring(16)
  } else if (idCard.length >= 15) {
    return idCard.substring(0, 6) + '*******' + idCard.substring(13)
  }
  return idCard
}

onMounted(() => {
  fetchVerificationInfo()
})
</script>

<style scoped lang="scss">
.verification-container {
  padding: 16px;

  .verification-status-card {
    background-color: #f9f9f9;
    border-radius: 8px;
    padding: 24px 16px;
    text-align: center;
    margin-bottom: 16px;
    
    .status-icon {
      font-size: 48px;
      color: #ff976a;
      margin-bottom: 12px;
    }
    
    h3 {
      margin: 0 0 8px;
      font-size: 18px;
      color: var(--text-color);
    }
    
    .status-desc {
      margin: 0;
      font-size: 14px;
      color: var(--text-color-light);
    }
    
    .submit-time {
      margin-top: 16px;
      font-size: 12px;
      color: var(--text-color-lighter);
    }
    
    .verification-info {
      margin-top: 16px;
      text-align: left;
      background-color: rgba(255, 255, 255, 0.6);
      padding: 12px;
      border-radius: 6px;
      
      p {
        margin: 8px 0;
        font-size: 14px;
        
        span {
          color: var(--text-color-light);
        }
      }
    }
    
    &.status-processing {
      background-color: #e3f4fd;
      
      .status-icon {
        color: #2196f3;
      }
    }
    
    &.status-success {
      background-color: #f0f9eb;
      
      .status-icon {
        color: #07c160;
      }
    }
    
    &.status-failed {
      background-color: #feefef;
      
      .status-icon {
        color: #ee0a24;
      }
      
      .van-button {
        margin-top: 16px;
      }
    }
  }
  
  .verification-form {
    .upload-group {
      margin-top: 16px;
    }
    
    .id-card-uploads {
      padding: 16px;
      
      .upload-item {
        margin-bottom: 24px;
        
        h4 {
          margin: 0 0 12px;
          font-size: 14px;
          font-weight: normal;
          color: var(--text-color);
        }
        
        .upload-tip {
          margin: 8px 0 0;
          font-size: 12px;
          color: var(--text-color-light);
        }
      }
      
      :deep(.van-uploader) {
        .van-uploader__wrapper {
          justify-content: center;
        }
        
        .van-uploader__upload {
          width: 160px;
          height: 100px;
        }
        
        .van-uploader__preview {
          width: 160px;
          height: 100px;
        }
      }
      
      .preview-cover {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.4);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #fff;
        
        .upload-status {
          margin-top: 8px;
          font-size: 12px;
        }
      }
    }
  }
  
  .privacy-statement {
    padding: 0 16px;
    margin-top: 24px;
    
    .link {
      color: var(--primary-color);
    }
  }
  
  .privacy-popup {
    height: 100%;
    display: flex;
    flex-direction: column;
    
    .popup-title {
      padding: 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid var(--border-color);
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: bold;
      }
    }
    
    .popup-content {
      flex: 1;
      overflow-y: auto;
      padding: 16px;
      
      h4 {
        margin: 16px 0 8px;
        font-size: 15px;
        color: var(--text-color);
      }
      
      p {
        margin: 8px 0;
        font-size: 14px;
        color: var(--text-color-light);
        line-height: 1.5;
      }
    }
    
    .popup-footer {
      padding: 16px;
      border-top: 1px solid var(--border-color);
    }
  }
}
</style> 