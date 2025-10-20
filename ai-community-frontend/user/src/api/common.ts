import request from '@/utils/request'

/**
 * 上传文件
 * @param file 文件对象
 */
export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post('/api/common/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

