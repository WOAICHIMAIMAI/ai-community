package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.VerificationAuditDTO;
import com.zheng.aicommunitybackend.domain.dto.VerificationPageQuery;
import com.zheng.aicommunitybackend.domain.dto.VerificationSubmitDTO;
import com.zheng.aicommunitybackend.domain.entity.UserVerification;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.result.PageResult;
import com.zheng.aicommunitybackend.domain.vo.AdminVerificationVO;
import com.zheng.aicommunitybackend.domain.vo.VerificationVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
* @author ZhengJJ
* @description 针对表【user_verification(用户实名认证信息表)】的数据库操作Service
* @createDate 2025-07-10 11:03:49
*/
public interface UserVerificationService extends IService<UserVerification> {

    /**
     * 提交实名认证申请
     *
     * @param verificationSubmitDTO 实名认证信息
     * @return 是否提交成功
     */
    boolean submitVerification(VerificationSubmitDTO verificationSubmitDTO);

    /**
     * 获取当前用户的认证信息
     *
     * @return 认证信息（脱敏后）
     */
    VerificationVO getCurrentUserVerification();
    
    /**
     * 上传身份证照片
     *
     * @param file 图片文件
     * @param type 图片类型：front-正面，back-反面
     * @return 图片URL
     * @throws IOException 如果上传失败
     */
    String uploadIdCardImage(MultipartFile file, String type) throws IOException;
    
    /**
     * 取消认证申请（仅认证中状态可取消）
     *
     * @return 是否取消成功
     */
    boolean cancelVerification();
    
    /**
     * 脱敏处理身份信息
     *
     * @param verification 原始认证信息
     * @return 脱敏后的认证信息
     */
    VerificationVO maskSensitiveInfo(UserVerification verification);
    
    /**
     * 管理员分页查询实名认证列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult adminListVerifications(VerificationPageQuery query);
    
    /**
     * 管理员获取实名认证详情
     *
     * @param verificationId 认证记录ID
     * @return 认证详情
     */
    AdminVerificationVO adminGetVerificationDetail(Long verificationId);
    
    /**
     * 管理员审核实名认证
     *
     * @param auditDTO 审核信息
     * @return 是否审核成功
     */
    boolean adminAuditVerification(VerificationAuditDTO auditDTO);
    
    /**
     * 管理员统计各状态的认证数量
     *
     * @return 状态统计结果
     */
    Integer[] adminCountVerificationStatus();
}
