<template>
  <div 
    :class="[
      'action-button', 
      `action-button--${variant}`,
      `action-button--${size}`,
      { 'action-button--disabled': disabled }
    ]"
    @click="handleClick"
  >
    <van-icon v-if="icon" :name="icon" class="action-icon" />
    <span class="action-text">{{ text }}</span>
    <van-icon v-if="showArrow" name="arrow" class="action-arrow" />
  </div>
</template>

<script setup lang="ts">
interface Props {
  text: string
  icon?: string
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'ghost'
  size?: 'small' | 'medium' | 'large'
  showArrow?: boolean
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default',
  size: 'medium',
  showArrow: true,
  disabled: false
})

const emit = defineEmits<{
  click: []
}>()

const handleClick = () => {
  if (!props.disabled) {
    emit('click')
  }
}
</script>

<style scoped lang="scss">
.action-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.5s;
  }

  &:hover::before {
    left: 100%;
  }

  &:active {
    transform: scale(0.98);
  }

  &--disabled {
    opacity: 0.5;
    cursor: not-allowed;
    
    &:hover::before {
      left: -100%;
    }
    
    &:active {
      transform: none;
    }
  }

  .action-icon {
    margin-right: 6px;
  }

  .action-arrow {
    margin-left: 6px;
    font-size: 12px;
    transition: transform 0.3s ease;
  }

  &:hover .action-arrow {
    transform: translateX(2px);
  }

  // 尺寸变体
  &--small {
    padding: 6px 12px;
    font-size: 12px;
    border-radius: 6px;

    .action-icon {
      font-size: 14px;
    }
  }

  &--medium {
    padding: 8px 16px;
    font-size: 13px;

    .action-icon {
      font-size: 16px;
    }
  }

  &--large {
    padding: 12px 20px;
    font-size: 14px;

    .action-icon {
      font-size: 18px;
    }
  }

  // 颜色变体
  &--default {
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border: 1px solid #e2e8f0;
    color: #475569;

    &:hover {
      background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
      border-color: #cbd5e1;
      box-shadow: 0 4px 12px rgba(71, 85, 105, 0.1);
    }
  }

  &--primary {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    border: 1px solid #2563eb;
    color: white;

    &:hover {
      background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
      border-color: #1d4ed8;
      box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
    }
  }

  &--success {
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    border: 1px solid #059669;
    color: white;

    &:hover {
      background: linear-gradient(135deg, #059669 0%, #047857 100%);
      border-color: #047857;
      box-shadow: 0 4px 12px rgba(5, 150, 105, 0.3);
    }
  }

  &--warning {
    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    border: 1px solid #d97706;
    color: white;

    &:hover {
      background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
      border-color: #b45309;
      box-shadow: 0 4px 12px rgba(217, 119, 6, 0.3);
    }
  }

  &--danger {
    background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
    border: 1px solid #dc2626;
    color: white;

    &:hover {
      background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
      border-color: #b91c1c;
      box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
    }
  }

  &--ghost {
    background: transparent;
    border: 1px solid #e2e8f0;
    color: #475569;

    &:hover {
      background: rgba(248, 250, 252, 0.8);
      border-color: #cbd5e1;
      box-shadow: 0 4px 12px rgba(71, 85, 105, 0.1);
    }
  }
}
</style>
