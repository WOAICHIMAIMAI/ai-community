package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.VerificationAuditDTO;
import com.zheng.aicommunitybackend.domain.dto.VerificationPageQuery;
import com.zheng.aicommunitybackend.domain.dto.VerificationSubmitDTO;
import com.zheng.aicommunitybackend.domain.entity.Users;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.AdminVerificationVO;
import com.zheng.aicommunitybackend.domain.vo.VerificationVO;
import com.zheng.aicommunitybackend.exception.BaseException;
import com.zheng.aicommunitybackend.mapper.UserVerificationMapper;
import com.zheng.aicommunitybackend.service.UserVerificationService;
import com.zheng.aicommunitybackend.domain.entity.UserVerification;
import com.zheng.aicommunitybackend.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author ZhengJJ
* @description 针对表【user_verification(用户实名认证信息表)】的数据库操作Service实现
* @createDate 2025-07-10 11:03:49
*/
@Service
@Slf4j
public class UserVerificationServiceImpl extends ServiceImpl<UserVerificationMapper, UserVerification>
    implements UserVerificationService {

    private final UsersService usersService;
    
    // 图片上传路径
    private final String uploadPath = "upload/idcard/";
    
    // 允许的图片类型
    private final String[] allowedImageTypes = {".jpg", ".jpeg", ".png"};
    
    // 最大文件大小（5MB）
    private final long maxFileSize = 5 * 1024 * 1024;

    public UserVerificationServiceImpl(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    @Transactional
    public boolean submitVerification(VerificationSubmitDTO verificationSubmitDTO) {
        // 获取当前用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BaseException("请先登录");
        }
        
        // 检查用户是否存在
        Users user = usersService.getById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        
        // 检查用户是否已经认证
        if (user.getIsVerified() != null && user.getIsVerified() == 1) {
            throw new BaseException("您已完成实名认证，无需重复提交");
        }
        
        // 检查是否有进行中的认证
        LambdaQueryWrapper<UserVerification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserVerification::getUserId, userId)
                .eq(UserVerification::getVerificationStatus, 1); // 认证中状态
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BaseException("您已提交认证申请，正在审核中");
        }
        
        // 检查身份证格式
        if (!isValidIdCard(verificationSubmitDTO.getIdCardNumber())) {
            throw new BaseException("身份证号格式不正确");
        }
        
        // 检查图片URL
        if (!StringUtils.hasText(verificationSubmitDTO.getIdCardFrontUrl()) || 
                !StringUtils.hasText(verificationSubmitDTO.getIdCardBackUrl())) {
            throw new BaseException("身份证照片不完整");
        }
        
        // 创建认证记录
        UserVerification verification = new UserVerification();
        verification.setUserId(userId);
        verification.setRealName(verificationSubmitDTO.getRealName());
        verification.setIdCardNumber(verificationSubmitDTO.getIdCardNumber());
        verification.setIdCardFrontUrl(verificationSubmitDTO.getIdCardFrontUrl());
        verification.setIdCardBackUrl(verificationSubmitDTO.getIdCardBackUrl());
        verification.setVerificationStatus(1); // 认证中状态
        verification.setSubmitTime(new Date());
        
        // 保存到数据库
        boolean saved = this.save(verification);
        
        if (saved) {
            // 更新用户表中的认证状态为认证中
            user.setIsVerified(1); // 认证中状态
            usersService.updateById(user);
        }
        
        return saved;
    }

    @Override
    public VerificationVO getCurrentUserVerification() {
        // 获取当前用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BaseException("请先登录");
        }
        
        // 查询最新的认证记录
        LambdaQueryWrapper<UserVerification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserVerification::getUserId, userId)
                .orderByDesc(UserVerification::getSubmitTime)
                .last("LIMIT 1");
        
        UserVerification verification = this.getOne(queryWrapper);
        
        // 如果没有认证记录，返回空对象
        if (verification == null) {
            return null;
        }
        
        // 脱敏处理后返回
        return maskSensitiveInfo(verification);
    }

    @Override
    public String uploadIdCardImage(MultipartFile file, String type) throws IOException {
        // 获取当前用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BaseException("请先登录");
        }
        
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BaseException("文件不能为空");
        }
        
        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BaseException("文件名不能为空");
        }
        
        // 检查文件类型
        boolean isValidType = false;
        for (String allowedType : allowedImageTypes) {
            if (originalFilename.toLowerCase().endsWith(allowedType)) {
                isValidType = true;
                break;
            }
        }
        
        if (!isValidType) {
            throw new BaseException("只支持JPG、JPEG、PNG格式的图片");
        }
        
        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new BaseException("文件大小不能超过5MB");
        }
        
        // 检查类型参数
        if (!"front".equals(type) && !"back".equals(type)) {
            throw new BaseException("图片类型参数错误，应为front或back");
        }
        
        // 创建保存目录
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // 生成新的文件名，避免文件名冲突
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
        // 组合用户ID和类型
        String finalFilename = userId + "_" + type + "_" + newFilename;
        Path targetPath = uploadDir.resolve(finalFilename);
        
        // 保存文件
        Files.copy(file.getInputStream(), targetPath);
        
        // 返回文件URL
        return "/upload/idcard/" + finalFilename;
    }

    @Override
    @Transactional
    public boolean cancelVerification() {
        // 获取当前用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BaseException("请先登录");
        }
        
        // 查询进行中的认证记录
        LambdaQueryWrapper<UserVerification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserVerification::getUserId, userId)
                .eq(UserVerification::getVerificationStatus, 1); // 认证中状态
        
        UserVerification verification = this.getOne(queryWrapper);
        
        if (verification == null) {
            throw new BaseException("没有进行中的认证申请");
        }
        
        // 修改状态为已取消（使用3表示认证失败）
        verification.setVerificationStatus(3);
        verification.setFailureReason("用户主动取消");
        verification.setCompleteTime(new Date());
        
        boolean updated = this.updateById(verification);
        
        if (updated) {
            // 更新用户表中的认证状态为未认证
            Users user = usersService.getById(userId);
            if (user != null) {
                user.setIsVerified(0); // 未认证状态
                usersService.updateById(user);
            }
        }
        
        return updated;
    }

    @Override
    public VerificationVO maskSensitiveInfo(UserVerification verification) {
        if (verification == null) {
            return null;
        }
        
        VerificationVO vo = new VerificationVO();
        BeanUtils.copyProperties(verification, vo);
        
        // 脱敏姓名，只显示第一个字，其余用*代替
        String realName = verification.getRealName();
        if (StringUtils.hasText(realName)) {
            if (realName.length() > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append(realName.charAt(0));
                for (int i = 1; i < realName.length(); i++) {
                    sb.append("*");
                }
                vo.setRealName(sb.toString());
            }
        }
        
        // 脱敏身份证号，只显示前4位和后4位，中间用*代替
        String idCardNumber = verification.getIdCardNumber();
        if (StringUtils.hasText(idCardNumber) && idCardNumber.length() >= 15) {
            vo.setIdCardNumber(idCardNumber.substring(0, 4) + "**********" + idCardNumber.substring(idCardNumber.length() - 4));
        }
        
        return vo;
    }
    
    /**
     * 简单验证身份证号格式是否正确
     */
    private boolean isValidIdCard(String idCardNumber) {
        if (!StringUtils.hasText(idCardNumber)) {
            return false;
        }
        
        // 简单验证长度和格式
        String pattern = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}(\\d|X|x)$";
        return idCardNumber.matches(pattern);
    }

    @Override
    public PageResult adminListVerifications(VerificationPageQuery query) {
        // 检查当前用户是否是管理员
        checkAdminPermission();
        
        LambdaQueryWrapper<UserVerification> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据用户ID查询
        if (query.getUserId() != null) {
            queryWrapper.eq(UserVerification::getUserId, query.getUserId());
        }
        
        // 根据真实姓名模糊查询
        if (StringUtils.hasText(query.getRealName())) {
            queryWrapper.like(UserVerification::getRealName, query.getRealName());
        }
        
        // 根据身份证号模糊查询
        if (StringUtils.hasText(query.getIdCardNumber())) {
            queryWrapper.like(UserVerification::getIdCardNumber, query.getIdCardNumber());
        }
        
        // 根据认证状态查询
        if (query.getVerificationStatus() != null) {
            queryWrapper.eq(UserVerification::getVerificationStatus, query.getVerificationStatus());
        }
        
        // 根据时间范围查询
        if (StringUtils.hasText(query.getStartTime()) && StringUtils.hasText(query.getEndTime())) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startDate = sdf.parse(query.getStartTime());
                Date endDate = sdf.parse(query.getEndTime());
                queryWrapper.between(UserVerification::getSubmitTime, startDate, endDate);
            } catch (ParseException e) {
                throw new BaseException("日期格式错误，正确格式：yyyy-MM-dd HH:mm:ss");
            }
        } else if (StringUtils.hasText(query.getStartTime())) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startDate = sdf.parse(query.getStartTime());
                queryWrapper.ge(UserVerification::getSubmitTime, startDate);
            } catch (ParseException e) {
                throw new BaseException("开始日期格式错误，正确格式：yyyy-MM-dd HH:mm:ss");
            }
        } else if (StringUtils.hasText(query.getEndTime())) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date endDate = sdf.parse(query.getEndTime());
                queryWrapper.le(UserVerification::getSubmitTime, endDate);
            } catch (ParseException e) {
                throw new BaseException("结束日期格式错误，正确格式：yyyy-MM-dd HH:mm:ss");
            }
        }
        
        // 排序
        if (StringUtils.hasText(query.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(query.getSortOrder());
            switch (query.getSortField()) {
                case "submitTime":
                    queryWrapper.orderBy(true, isAsc, UserVerification::getSubmitTime);
                    break;
                case "completeTime":
                    queryWrapper.orderBy(true, isAsc, UserVerification::getCompleteTime);
                    break;
                case "verificationStatus":
                    queryWrapper.orderBy(true, isAsc, UserVerification::getVerificationStatus);
                    break;
                default:
                    queryWrapper.orderByDesc(UserVerification::getSubmitTime);
            }
        } else {
            queryWrapper.orderByDesc(UserVerification::getSubmitTime);
        }
        
        // 执行查询
        Page<UserVerification> page = this.page(new Page<>(query.getPage(), query.getPageSize()), queryWrapper);
        
        // 转换结果
        List<AdminVerificationVO> voList = page.getRecords().stream()
                .map(this::convertToAdminVO)
                .collect(Collectors.toList());
        
        return new PageResult(page.getTotal(), voList);
    }

    @Override
    public AdminVerificationVO adminGetVerificationDetail(Long verificationId) {
        // 检查当前用户是否是管理员
        checkAdminPermission();
        
        // 查询认证记录
        UserVerification verification = this.getById(verificationId);
        if (verification == null) {
            throw new BaseException("认证记录不存在");
        }
        
        // 转换为VO
        return convertToAdminVO(verification);
    }

    @Override
    @Transactional
    public boolean adminAuditVerification(VerificationAuditDTO auditDTO) {
        // 检查当前用户是否是管理员
        checkAdminPermission();
        
        // 参数校验
        if (auditDTO == null || auditDTO.getVerificationId() == null) {
            throw new BaseException("参数不能为空");
        }
        
        // 审核结果校验
        if (auditDTO.getApproved() == null) {
            throw new BaseException("审核结果不能为空");
        }
        
        // 如果拒绝，必须填写拒绝原因
        if (!auditDTO.getApproved() && !StringUtils.hasText(auditDTO.getRejectReason())) {
            throw new BaseException("拒绝时必须填写拒绝原因");
        }
        
        // 查询认证记录
        UserVerification verification = this.getById(auditDTO.getVerificationId());
        if (verification == null) {
            throw new BaseException("认证记录不存在");
        }
        
        // 检查状态是否为认证中
        if (verification.getVerificationStatus() != 1) {
            throw new BaseException("只能审核认证中的申请");
        }
        
        // 更新认证状态
        verification.setVerificationStatus(auditDTO.getApproved() ? 2 : 3); // 2-已认证 3-认证失败
        if (!auditDTO.getApproved()) {
            verification.setFailureReason(auditDTO.getRejectReason());
        }
        verification.setCompleteTime(new Date());
        
        boolean updated = this.updateById(verification);
        
        if (updated) {
            // 更新用户表中的认证状态
            Users user = usersService.getById(verification.getUserId());
            if (user != null) {
                user.setIsVerified(auditDTO.getApproved() ? 2 : 0); // 2-已认证 0-未认证
                usersService.updateById(user);
            }
        }
        
        return updated;
    }

    @Override
    public Integer[] adminCountVerificationStatus() {
        // 检查当前用户是否是管理员
        checkAdminPermission();
        
        // 统计各状态数量，结果顺序：待审核、已通过、已拒绝、总数
        Integer[] result = new Integer[4];
        
        LambdaQueryWrapper<UserVerification> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(UserVerification::getVerificationStatus, 1); // 认证中
        result[0] = Math.toIntExact(this.count(pendingWrapper));
        
        LambdaQueryWrapper<UserVerification> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(UserVerification::getVerificationStatus, 2); // 已认证
        result[1] = Math.toIntExact(this.count(approvedWrapper));
        
        LambdaQueryWrapper<UserVerification> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(UserVerification::getVerificationStatus, 3); // 认证失败
        result[2] = Math.toIntExact(this.count(rejectedWrapper));
        
        result[3] = Math.toIntExact(this.count()); // 总数
        
        return result;
    }
    
    /**
     * 将认证记录转换为管理员VO
     */
    private AdminVerificationVO convertToAdminVO(UserVerification verification) {
        if (verification == null) {
            return null;
        }
        
        AdminVerificationVO vo = new AdminVerificationVO();
        BeanUtils.copyProperties(verification, vo);
        
        // 获取用户信息
        Users user = usersService.getById(verification.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setPhone(user.getPhone());
        }
        
        return vo;
    }
    
    /**
     * 检查当前用户是否有管理员权限
     */
    private void checkAdminPermission() {
        Integer userRole = UserContext.getUserRole();
        if (userRole == null || userRole != 1) {
            throw new BaseException("无权限操作，请联系管理员");
        }
    }
}




