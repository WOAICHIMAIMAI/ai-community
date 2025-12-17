package com.zheng.aicommunitybackend.controller;

import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.util.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传:{}", file.getOriginalFilename());
        
        try {
            // 校验文件
            if (file == null || file.isEmpty()) {
                return Result.error("文件不能为空！");
            }
            
            //原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return Result.error("文件名不合法！");
            }
            
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = UUID.randomUUID() + extension;
            
            // 上传文件到OSS
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            log.info("文件上传成功: {}", filePath);
            return Result.success(filePath);
            
        } catch (IOException e) {
            log.error("读取文件失败：{}", e.getMessage(), e);
            return Result.error("读取文件失败！");
        } catch (RuntimeException e) {
            log.error("文件上传到OSS失败：{}", e.getMessage(), e);
            return Result.error("文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("文件上传异常：{}", e.getMessage(), e);
            return Result.error("文件上传失败！");
        }
    }

}
