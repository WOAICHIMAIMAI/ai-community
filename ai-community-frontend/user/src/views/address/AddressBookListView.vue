<template>
  <div class="address-book-container">
    <van-nav-bar
      title="地址簿"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <!-- 地址列表 -->
      <div class="address-list">
        <template v-if="loading">
          <van-skeleton title :row="3" v-for="i in 3" :key="i" />
        </template>
        
        <template v-else-if="addressList.length > 0">
          <div
            v-for="address in addressList"
            :key="address.id"
            class="address-item"
            :class="{ selected: selectMode && selectedAddressId === address.id }"
            @click="handleAddressClick(address)"
          >
            <div class="address-header">
              <div class="user-info">
                <span class="name">{{ address.name }}</span>
                <span class="phone">{{ address.phone }}</span>
              </div>
              <van-tag v-if="address.isDefault === 1" type="danger" size="small">默认</van-tag>
            </div>
            
            <div class="address-detail">
              <div v-if="address.label" class="address-label">
                <van-tag type="primary" plain size="small">{{ address.label }}</van-tag>
              </div>
              <div class="address-text">
                {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
              </div>
            </div>
            
            <div class="address-actions" v-if="!selectMode">
              <van-button size="small" @click.stop="handleEdit(address)">编辑</van-button>
              <van-button 
                size="small" 
                type="primary" 
                plain
                @click.stop="handleSetDefault(address.id)"
                v-if="address.isDefault !== 1"
              >
                设为默认
              </van-button>
              <van-button 
                size="small" 
                type="danger" 
                plain
                @click.stop="handleDelete(address.id)"
              >
                删除
              </van-button>
            </div>
          </div>
        </template>
        
        <template v-else>
          <div class="empty-list">
            <van-empty image="search" description="暂无地址信息" />
          </div>
        </template>
      </div>
    </div>
    
    <!-- 新增地址按钮 -->
    <div class="footer-button">
      <van-button type="primary" block @click="handleAdd">
        新增地址
      </van-button>
    </div>
    
    <!-- 编辑/新增地址弹窗 -->
    <van-popup v-model:show="showEditPopup" position="bottom" :style="{ height: '70%' }">
      <div class="edit-popup">
        <div class="popup-header">
          <span>{{ editingAddress.id ? '编辑地址' : '新增地址' }}</span>
          <van-icon name="cross" @click="showEditPopup = false" />
        </div>
        
        <van-form @submit="handleSubmit">
          <van-cell-group inset>
            <van-field
              v-model="editingAddress.name"
              label="联系人"
              placeholder="请输入联系人姓名"
              :rules="[{ required: true, message: '请输入联系人' }]"
            />
            <van-field
              v-model="editingAddress.phone"
              label="联系电话"
              placeholder="请输入联系电话"
              :rules="[
                { required: true, message: '请输入联系电话' },
                { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
              ]"
            />
            <van-field
              v-model="areaText"
              is-link
              readonly
              label="所在地区"
              placeholder="请选择省市区"
              @click="showAreaPicker = true"
              :rules="[{ required: true, message: '请选择所在地区' }]"
            />
            <van-field
              v-model="editingAddress.detail"
              label="详细地址"
              placeholder="请输入详细地址"
              type="textarea"
              rows="2"
              :rules="[{ required: true, message: '请输入详细地址' }]"
            />
            <van-field
              v-model="editingAddress.postalCode"
              label="邮政编码"
              placeholder="请输入邮政编码（选填）"
              maxlength="6"
            />
            <van-field
              v-model="editingAddress.label"
              label="地址标签"
              placeholder="如：家/公司/学校（选填）"
            />
            <van-field name="isDefault">
              <template #input>
                <van-checkbox v-model="isDefaultChecked">设为默认地址</van-checkbox>
              </template>
            </van-field>
          </van-cell-group>
          
          <div class="form-button">
            <van-button round block type="primary" native-type="submit">
              保存
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>
    
    <!-- 省市区选择器 -->
    <van-popup v-model:show="showAreaPicker" position="bottom">
      <van-area
        title="选择地区"
        :area-list="areaList"
        @confirm="onAreaConfirm"
        @cancel="showAreaPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { areaList } from '@vant/area-data'
import { 
  getAddressBookList, 
  addAddressBook, 
  updateAddressBook, 
  deleteAddressBook,
  setDefaultAddress,
  type AddressBook 
} from '@/api/addressBook'

const router = useRouter()
const route = useRoute()

// 是否为选择模式（从其他页面跳转过来选择地址）
const selectMode = computed(() => route.query.select === 'true')
const selectedAddressId = ref<number | null>(null)

// 响应式数据
const loading = ref(false)
const addressList = ref<AddressBook[]>([])
const showEditPopup = ref(false)
const showAreaPicker = ref(false)
const editingAddress = ref<Partial<AddressBook>>({})
const isDefaultChecked = ref(false)
const areaText = ref('')

// 页面初始化
onMounted(() => {
  loadAddressList()
})

// 返回上一页
const onClickLeft = () => {
  router.back()
}

// 加载地址列表
const loadAddressList = async () => {
  try {
    loading.value = true
    const response = await getAddressBookList()
    if (response.code === 200) {
      addressList.value = response.data
    } else {
      showToast(response.message || '加载失败')
    }
  } catch (error) {
    console.error('加载地址列表失败:', error)
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

// 处理地址点击
const handleAddressClick = (address: AddressBook) => {
  if (selectMode.value) {
    // 选择模式：选中地址并返回
    selectedAddressId.value = address.id
    router.back()
    // 通过事件或状态传递选中的地址
    window.sessionStorage.setItem('selectedAddress', JSON.stringify(address))
  }
}

// 省市区选择确认
const onAreaConfirm = ({ selectedOptions }: any) => {
  showAreaPicker.value = false
  if (selectedOptions && selectedOptions.length === 3) {
    editingAddress.value.province = selectedOptions[0].text
    editingAddress.value.city = selectedOptions[1].text
    editingAddress.value.district = selectedOptions[2].text
    areaText.value = `${selectedOptions[0].text} ${selectedOptions[1].text} ${selectedOptions[2].text}`
  }
}

// 新增地址
const handleAdd = () => {
  editingAddress.value = {}
  isDefaultChecked.value = false
  areaText.value = ''
  showEditPopup.value = true
}

// 编辑地址
const handleEdit = (address: AddressBook) => {
  editingAddress.value = { ...address }
  isDefaultChecked.value = address.isDefault === 1
  areaText.value = address.province && address.city && address.district 
    ? `${address.province} ${address.city} ${address.district}`
    : ''
  showEditPopup.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    // 验证省市区是否已选择
    if (!editingAddress.value.province || !editingAddress.value.city || !editingAddress.value.district) {
      showToast('请选择所在地区')
      return
    }
    
    const addressData = {
      ...editingAddress.value,
      isDefault: isDefaultChecked.value ? 1 : 0
    }
    
    let response
    if (editingAddress.value.id) {
      // 编辑
      response = await updateAddressBook(editingAddress.value.id, addressData)
    } else {
      // 新增
      response = await addAddressBook(addressData)
    }
    
    if (response.code === 200) {
      showToast('保存成功')
      showEditPopup.value = false
      loadAddressList()
    } else {
      showToast(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存地址失败:', error)
    showToast('保存失败')
  }
}

// 设置默认地址
const handleSetDefault = async (id: number) => {
  try {
    const response = await setDefaultAddress(id)
    if (response.code === 200) {
      showToast('设置成功')
      loadAddressList()
    } else {
      showToast(response.message || '设置失败')
    }
  } catch (error) {
    console.error('设置默认地址失败:', error)
    showToast('设置失败')
  }
}

// 删除地址
const handleDelete = async (id: number) => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要删除这个地址吗？'
    })
    
    const response = await deleteAddressBook(id)
    if (response.code === 200) {
      showToast('删除成功')
      loadAddressList()
    } else {
      showToast(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除地址失败:', error)
      showToast('删除失败')
    }
  }
}
</script>

<style scoped lang="scss">
.address-book-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  
  .content {
    padding-top: 46px;
    padding-bottom: 70px;
  }
  
  .address-list {
    padding: 16px;
    
    .address-item {
      background: white;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 12px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
      transition: all 0.3s;
      
      &.selected {
        border: 2px solid #1989fa;
        background-color: #f0f9ff;
      }
      
      &:active {
        transform: scale(0.98);
      }
      
      .address-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        
        .user-info {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .name {
            font-size: 16px;
            font-weight: 600;
            color: #333;
          }
          
          .phone {
            font-size: 14px;
            color: #666;
          }
        }
      }
      
      .address-detail {
        font-size: 14px;
        color: #666;
        line-height: 1.6;
        margin-bottom: 12px;
        
        .address-label {
          margin-bottom: 6px;
        }
        
        .address-text {
          line-height: 1.6;
        }
      }
      
      .address-actions {
        display: flex;
        gap: 8px;
        justify-content: flex-end;
      }
    }
    
    .empty-list {
      padding: 60px 0;
      text-align: center;
    }
  }
  
  .footer-button {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 12px 16px;
    background: white;
    box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.05);
  }
  
  .edit-popup {
    height: 100%;
    display: flex;
    flex-direction: column;
    background-color: #f5f5f5;
    
    .popup-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      background: white;
      font-size: 16px;
      font-weight: 600;
      border-bottom: 1px solid #eee;
      
      .van-icon {
        font-size: 20px;
        color: #999;
      }
    }
    
    .van-form {
      flex: 1;
      overflow-y: auto;
      padding: 16px 0;
    }
    
    .form-button {
      padding: 16px;
    }
  }
}
</style>

