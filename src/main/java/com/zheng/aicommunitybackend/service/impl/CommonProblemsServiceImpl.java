package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.dto.CommonProblemCategoryDTO;
import com.zheng.aicommunitybackend.domain.dto.CommonProblemQueryDTO;
import com.zheng.aicommunitybackend.domain.entity.CommonProblems;
import com.zheng.aicommunitybackend.domain.enums.ProblemCategoryEnum;
import com.zheng.aicommunitybackend.domain.vo.CommonProblemVO;
import com.zheng.aicommunitybackend.exception.BaseException;
import com.zheng.aicommunitybackend.mapper.CommonProblemsMapper;
import com.zheng.aicommunitybackend.service.CommonProblemsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author ZhengJJ
* @description 针对表【common_problems】的数据库操作Service实现
* @createDate 2025-07-21 20:22:08
*/
@Service
@Slf4j
public class CommonProblemsServiceImpl extends ServiceImpl<CommonProblemsMapper, CommonProblems>
    implements CommonProblemsService{

    @Override
    public List<CommonProblemCategoryDTO> getProblemCategories() {
        log.info("获取问题分类列表");

        // 查询所有问题，按分类统计数量
        List<CommonProblems> allProblems = list();

        // 按分类分组统计
        Map<Integer, Long> categoryCountMap = allProblems.stream()
                .collect(Collectors.groupingBy(
                        CommonProblems::getType,
                        Collectors.counting()
                ));

        // 构建分类DTO列表
        List<CommonProblemCategoryDTO> categories = new ArrayList<>();
        for (ProblemCategoryEnum categoryEnum : ProblemCategoryEnum.values()) {
            CommonProblemCategoryDTO dto = new CommonProblemCategoryDTO();
            dto.setType(categoryEnum.getCode());
            dto.setCategoryName(categoryEnum.getDescription());
            dto.setProblemCount(categoryCountMap.getOrDefault(categoryEnum.getCode(), 0L));
            categories.add(dto);
        }

        return categories;
    }

    @Override
    public IPage<CommonProblemVO> getProblemsPage(CommonProblemQueryDTO queryDTO) {
        log.info("根据条件查询问题列表: {}", queryDTO);

        // 构建查询条件
        LambdaQueryWrapper<CommonProblems> wrapper = new LambdaQueryWrapper<>();

        // 按分类查询
        if (queryDTO.getType() != null) {
            wrapper.eq(CommonProblems::getType, queryDTO.getType());
        }

        // 是否只查询置顶问题
        if (queryDTO.getOnlyPriority() != null && queryDTO.getOnlyPriority()) {
            wrapper.eq(CommonProblems::getPriority, 1);
        }

        // 排序：置顶优先，然后按ID排序
        wrapper.orderByDesc(CommonProblems::getPriority)
               .orderByAsc(CommonProblems::getId);

        // 分页查询
        Page<CommonProblems> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<CommonProblems> problemPage = page(page, wrapper);

        // 转换为VO
        IPage<CommonProblemVO> voPage = new Page<>();
        BeanUtils.copyProperties(problemPage, voPage);

        List<CommonProblemVO> voList = problemPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public CommonProblemVO getProblemDetail(Integer id) {
        log.info("根据ID查询问题详情: {}", id);

        CommonProblems problem = getById(id);
        if (problem == null) {
            throw new BaseException("问题不存在");
        }

        return convertToVO(problem);
    }

    /**
     * 转换为VO对象
     */
    private CommonProblemVO convertToVO(CommonProblems problem) {
        CommonProblemVO vo = new CommonProblemVO();
        BeanUtils.copyProperties(problem, vo);

        // 设置分类名称
        vo.setCategoryName(ProblemCategoryEnum.getDescriptionByCode(problem.getType()));

        // 设置是否置顶
        vo.setIsTop(problem.getPriority() != null && problem.getPriority() == 1);

        return vo;
    }




