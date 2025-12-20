<template>
  <div class="service-create">
    <van-nav-bar 
      title="创建服务" 
      left-arrow 
      @click-left="goBack" 
      fixed 
      placeholder
    />
    
    <div class="content">
      <van-form @submit="onSubmit" ref="formRef">
        <!-- 服务基本信息 -->
        <van-cell-group inset title="基本信息">
          <van-field
            v-model="form.serviceName"
            name="serviceName"
            label="服务名称"
            placeholder="请输入服务名称"
            :rules="[{ required: true, message: '请输入服务名称' }]"
          />
          
          <van-field
            :model-value="getServiceTypeName()"
            name="serviceType"
            label="服务类型"
            placeholder="请选择服务类型"
            readonly
            is-link
            @click="showServiceTypePicker = true"
            :rules="[{ required: true, message: '请选择服务类型' }]"
          />
          
          <van-field
            v-model="form.description"
            name="description"
            label="服务描述"
            type="textarea"
            placeholder="请详细描述您提供的服务内容"
            rows="4"
            maxlength="500"
            show-word-limit
            :rules="[{ required: true, message: '请输入服务描述' }]"
          />
        </van-cell-group>

        <!-- 价格信息 -->
        <van-cell-group inset title="价格信息">
          <van-field
            v-model="form.basePrice"
            name="basePrice"
            label="服务价格"
            type="number"
            placeholder="请输入服务价格"
            :rules="[
              { required: true, message: '请输入服务价格' },
              { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入正确的价格格式' }
            ]"
          >
            <template #button>
              <span class="price-unit">元</span>
            </template>
          </van-field>
          
          <van-field
            v-model="form.unit"
            name="unit"
            label="计价单位"
            placeholder="请输入计价单位"
            :rules="[{ required: true, message: '请输入计价单位' }]"
          >
            <template #extra>
              <span class="unit-example">如：次、小时、平方米</span>
            </template>
          </van-field>
        </van-cell-group>

        <!-- 提交按钮 -->
        <div class="submit-section">
          <van-button 
            round 
            block 
            type="primary" 
            native-type="submit" 
            :loading="loading"
            size="large"
          >
            提交审核
          </van-button>
          <p class="submit-tip">提交后将进入平台审核，审核通过后即可上线服务</p>
        </div>
      </van-form>
    </div>

    <!-- 服务类型选择器 -->
    <van-popup v-model:show="showServiceTypePicker" position="bottom" round>
      <van-picker
        :columns="serviceTypeColumns"
        @confirm="onServiceTypeConfirm"
        @cancel="showServiceTypePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import { createService, type ServiceCreateData } from '@/api/appointment'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const showServiceTypePicker = ref(false)

// 服务类型选项
const serviceTypeColumns = [
  { text: '家政保洁', value: 'cleaning', icon: 'brush-o' },
  { text: '维修服务', value: 'repair', icon: 'setting-o' },
  { text: '家电维修', value: 'appliance', icon: 'tv-o' },
  { text: '搬家服务', value: 'moving', icon: 'logistics' },
  { text: '家教服务', value: 'tutoring', icon: 'friends-o' },
  { text: '园艺绿化', value: 'gardening', icon: 'flower-o' },
  { text: '除虫消杀', value: 'pest', icon: 'delete-o' }
]

// 表单数据
const form = reactive<{
  serviceType: string
  serviceName: string
  description: string
  icon: string
  basePrice: string
  unit: string
}>({
  serviceType: '',
  serviceName: '',
  description: '',
  icon: '',
  basePrice: '',
  unit: '次'
})

// 获取服务类型显示名称
const getServiceTypeName = () => {
  const selected = serviceTypeColumns.find(item => item.value === form.serviceType)
  return selected?.text || ''
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 选择服务类型
const onServiceTypeConfirm = ({ selectedOptions }: any) => {
  const selected = selectedOptions[0]
  form.serviceType = selected.value
  form.icon = selected.icon
  showServiceTypePicker.value = false
}

// 提交表单
const onSubmit = async () => {
  loading.value = true
  
  try {
    // 获取服务类型的显示名称
    const selectedType = serviceTypeColumns.find(item => item.value === form.serviceType)
    
    const data: ServiceCreateData = {
      serviceType: selectedType?.text || '',
      serviceName: form.serviceName,
      description: form.description,
      icon: form.icon,
      basePrice: parseFloat(form.basePrice),
      unit: form.unit
    }
    
    const res = await createService(data)
    
    if (res.code === 200) {
      showSuccessToast('提交成功，等待审核')
      setTimeout(() => {
        router.back()
      }, 1500)
    } else {
      showFailToast(res.message || '提交失败')
    }
  } catch (error) {
    console.error('创建服务失败:', error)
    showFailToast('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.service-create {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 20px;

  .content {
    padding: 16px 0;

    .van-cell-group {
      margin-bottom: 16px;
    }

    .price-unit {
      color: #969799;
      font-size: 14px;
    }

    .unit-example,
    .area-example,
    .time-example {
      font-size: 12px;
      color: #969799;
    }

    .upload-section {
      padding: 16px;

      .upload-tip {
        margin-top: 12px;
        font-size: 12px;
        color: #969799;
        text-align: center;
      }

      :deep(.van-uploader) {
        .van-uploader__wrapper {
          justify-content: flex-start;
        }

        .van-uploader__upload {
          width: 80px;
          height: 80px;
          margin: 0 8px 8px 0;
        }

        .van-uploader__preview {
          width: 80px;
          height: 80px;
          margin: 0 8px 8px 0;
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

    .submit-section {
      padding: 24px 16px;

      .van-button {
        height: 48px;
        font-size: 16px;
        font-weight: 600;
      }

      .submit-tip {
        margin-top: 12px;
        font-size: 12px;
        color: #969799;
        text-align: center;
        line-height: 1.5;
      }
    }
  }
}

// 深色模式支持
@media (prefers-color-scheme: dark) {
  .service-create {
    background: #1a1a1a;

    .unit-example,
    .area-example,
    .time-example,
    .upload-tip,
    .submit-tip {
      color: #b0b0b0;
    }
  }
}
</style>

