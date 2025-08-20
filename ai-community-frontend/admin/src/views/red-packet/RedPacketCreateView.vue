<template>
  <div class="red-packet-create">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>创建红包活动</h2>
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <!-- 创建表单 -->
    <el-card>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 800px"
      >
        <el-form-item label="活动名称" prop="activityName">
          <el-input
            v-model="form.activityName"
            placeholder="请输入活动名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="活动描述" prop="activityDesc">
          <el-input
            v-model="form.activityDesc"
            type="textarea"
            placeholder="请输入活动描述"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="红包总金额" prop="totalAmount">
              <el-input-number
                v-model="form.totalAmount"
                :min="0.01"
                :max="100000"
                :precision="2"
                :step="1"
                style="width: 100%"
              />
              <span class="form-tip">元</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="红包数量" prop="totalCount">
              <el-input-number
                v-model="form.totalCount"
                :min="1"
                :max="10000"
                :step="1"
                style="width: 100%"
              />
              <span class="form-tip">个</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
                :disabled-date="disabledStartDate"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
                :disabled-date="disabledEndDate"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="分配算法" prop="algorithm">
          <el-radio-group v-model="form.algorithm">
            <el-radio value="DOUBLE_AVERAGE">二倍均值法（推荐）</el-radio>
            <el-radio value="RANDOM">随机分配</el-radio>
            <el-radio value="EVENLY">均匀分配</el-radio>
          </el-radio-group>
          <div class="form-tip">
            <p>• 二倍均值法：微信红包算法，保证公平性和随机性</p>
            <p>• 随机分配：简单随机分配，适用于对公平性要求不高的场景</p>
            <p>• 均匀分配：尽可能平均分配，适用于公平性要求很高的场景</p>
          </div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最小金额" prop="minAmount">
              <el-input-number
                v-model="form.minAmount"
                :min="1"
                :max="10000"
                :step="1"
                style="width: 100%"
              />
              <span class="form-tip">分</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大金额" prop="maxAmount">
              <el-input-number
                v-model="form.maxAmount"
                :min="1"
                :max="1000000"
                :step="1"
                style="width: 100%"
              />
              <span class="form-tip">分（可选）</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="立即开始">
          <el-switch
            v-model="form.startImmediately"
            active-text="是"
            inactive-text="否"
          />
          <div class="form-tip">开启后活动将立即开始，忽略开始时间设置</div>
        </el-form-item>

        <!-- 预览信息 -->
        <el-form-item label="预览信息">
          <el-card class="preview-card">
            <div class="preview-item">
              <span class="label">平均金额：</span>
              <span class="value">{{ averageAmount }}元</span>
            </div>
            <div class="preview-item">
              <span class="label">活动时长：</span>
              <span class="value">{{ activityDuration }}</span>
            </div>
            <div class="preview-item">
              <span class="label">预计参与：</span>
              <span class="value">{{ form.totalCount }}人</span>
            </div>
          </el-card>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            创建活动
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import redPacketApi, { type RedPacketActivityCreateDTO } from '@/api/red-packet'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)

// 表单数据
const form = reactive<RedPacketActivityCreateDTO>({
  activityName: '',
  activityDesc: '',
  totalAmount: 100,
  totalCount: 10,
  startTime: '',
  endTime: '',
  algorithm: 'DOUBLE_AVERAGE',
  minAmount: 1,
  maxAmount: undefined,
  startImmediately: false
})

// 表单验证规则
const rules: FormRules = {
  activityName: [
    { required: true, message: '请输入活动名称', trigger: 'blur' },
    { min: 2, max: 100, message: '活动名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  activityDesc: [
    { max: 500, message: '活动描述不能超过 500 个字符', trigger: 'blur' }
  ],
  totalAmount: [
    { required: true, message: '请输入红包总金额', trigger: 'blur' },
    { type: 'number', min: 0.01, max: 100000, message: '金额范围在 0.01 到 100000 元', trigger: 'blur' }
  ],
  totalCount: [
    { required: true, message: '请输入红包数量', trigger: 'blur' },
    { type: 'number', min: 1, max: 10000, message: '数量范围在 1 到 10000 个', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  algorithm: [
    { required: true, message: '请选择分配算法', trigger: 'change' }
  ],
  minAmount: [
    { required: true, message: '请输入最小金额', trigger: 'blur' },
    { type: 'number', min: 1, max: 10000, message: '最小金额范围在 1 到 10000 分', trigger: 'blur' }
  ]
}

// 计算属性
const averageAmount = computed(() => {
  if (form.totalAmount && form.totalCount) {
    return (form.totalAmount / form.totalCount).toFixed(2)
  }
  return '0.00'
})

const activityDuration = computed(() => {
  if (form.startTime && form.endTime) {
    const start = new Date(form.startTime)
    const end = new Date(form.endTime)
    const diff = end.getTime() - start.getTime()
    const hours = Math.floor(diff / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    return `${hours}小时${minutes}分钟`
  }
  return '未设置'
})

// 日期禁用函数
const disabledStartDate = (time: Date) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

const disabledEndDate = (time: Date) => {
  if (form.startTime) {
    return time.getTime() < new Date(form.startTime).getTime()
  }
  return time.getTime() < Date.now()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    // 验证时间
    if (!form.startImmediately && form.endTime) {
      const start = new Date(form.startTime)
      const end = new Date(form.endTime)
      if (end.getTime() <= start.getTime()) {
        ElMessage.error('结束时间必须晚于开始时间')
        return
      }
    }

    // 验证平均金额
    const avgAmountCents = (form.totalAmount * 100) / form.totalCount
    if (avgAmountCents < form.minAmount) {
      ElMessage.error('平均金额不能小于最小金额')
      return
    }

    submitting.value = true

    const response = await redPacketApi.createActivity(form)
    if (response.code === 200) {
      ElMessage.success('创建成功')
      router.push('/red-packet')
    } else {
      ElMessage.error(response.message || '创建失败')
    }
  } catch (error) {
    console.error('创建红包活动失败:', error)
    if (error !== 'validation failed') {
      ElMessage.error('创建失败')
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
}

// 返回列表
const goBack = () => {
  router.push('/red-packet')
}
</script>

<style scoped lang="scss">
.red-packet-create {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: #303133;
    }
  }

  .form-tip {
    margin-left: 10px;
    color: #909399;
    font-size: 12px;

    p {
      margin: 2px 0;
    }
  }

  .preview-card {
    background: #f8f9fa;
    border: 1px solid #e9ecef;

    .preview-item {
      display: flex;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        width: 80px;
        color: #666;
      }

      .value {
        color: #333;
        font-weight: bold;
      }
    }
  }
}
</style>
