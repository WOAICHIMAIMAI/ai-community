<template>
  <div class="more-button" @click="$emit('click')">
    <span class="more-text">{{ text }}</span>
    <van-icon name="arrow" class="more-icon" />
  </div>
</template>

<script setup lang="ts">
interface Props {
  text?: string
}

withDefaults(defineProps<Props>(), {
  text: '查看全部'
})

defineEmits<{
  click: []
}>()
</script>

<style scoped lang="scss">
.more-button {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1px solid #bae6fd;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 13px;
  color: #0369a1;
  font-weight: 500;

  &:hover {
    background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
    border-color: #7dd3fc;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(14, 165, 233, 0.15);
  }

  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 6px rgba(14, 165, 233, 0.2);
  }

  .more-text {
    margin-right: 4px;
  }

  .more-icon {
    font-size: 12px;
    transition: transform 0.3s ease;
  }

  &:hover .more-icon {
    transform: translateX(2px);
  }
}

// 不同主题色的变体
.more-button.primary {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  border-color: #93c5fd;
  color: #1d4ed8;

  &:hover {
    background: linear-gradient(135deg, #bfdbfe 0%, #93c5fd 100%);
    border-color: #60a5fa;
  }
}

.more-button.success {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  border-color: #86efac;
  color: #15803d;

  &:hover {
    background: linear-gradient(135deg, #bbf7d0 0%, #86efac 100%);
    border-color: #4ade80;
  }
}

.more-button.warning {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-color: #fcd34d;
  color: #d97706;

  &:hover {
    background: linear-gradient(135deg, #fde68a 0%, #fcd34d 100%);
    border-color: #f59e0b;
  }
}
</style>
