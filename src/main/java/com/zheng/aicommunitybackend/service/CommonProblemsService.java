package com.zheng.aicommunitybackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.dto.CommonProblemCategoryDTO;
import com.zheng.aicommunitybackend.domain.dto.CommonProblemQueryDTO;
import com.zheng.aicommunitybackend.domain.entity.CommonProblems;
import com.zheng.aicommunitybackend.domain.vo.CommonProblemVO;

import java.util.List;

/**
* @author ZhengJJ
* @description 针对表【common_problems】的数据库操作Service
* @createDate 2025-07-21 20:22:08
*/
public interface CommonProblemsService extends IService<CommonProblems> {

    /**
     * 获取问题分类列表
     * @return 分类列表
     */
    List<CommonProblemCategoryDTO> getProblemCategories();

    /**
     * 根据条件查询问题列表
     * @param queryDTO 查询条件
     * @return 分页问题列表
     */
    IPage<CommonProblemVO> getProblemsPage(CommonProblemQueryDTO queryDTO);

    /**
     * 根据问题ID查询问题详情
     * @param id 问题ID
     * @return 问题详情
     */
    CommonProblemVO getProblemDetail(Integer id);
}
