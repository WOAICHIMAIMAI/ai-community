package com.zheng.aicommunitybackend.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zheng.aicommunitybackend.common.Result;
import com.zheng.aicommunitybackend.domain.dto.CommonProblemCategoryDTO;
import com.zheng.aicommunitybackend.domain.dto.CommonProblemQueryDTO;
import com.zheng.aicommunitybackend.domain.vo.CommonProblemVO;
import com.zheng.aicommunitybackend.service.CommonProblemsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 常见问题接口
 */
@RestController
@RequestMapping("/user/common-problems")
@Slf4j
@Tag(name = "常见问题接口", description = "常见问题相关接口")
public class CommonProblemsController {

    @Autowired
    private CommonProblemsService commonProblemsService;

    /**
     * 获取问题分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取问题分类列表", description = "获取所有问题分类及每个分类下的问题数量")
    public Result<List<CommonProblemCategoryDTO>> getProblemCategories() {
        log.info("获取问题分类列表");
        List<CommonProblemCategoryDTO> categories = commonProblemsService.getProblemCategories();
        return Result.success(categories);
    }

    /**
     * 根据条件查询问题列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询问题列表", description = "根据分类和优先级查询问题列表")
    public Result<IPage<CommonProblemVO>> getProblemsPage(
            @Parameter(description = "问题分类类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "是否只查询置顶问题") @RequestParam(required = false) Boolean onlyPriority,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        log.info("查询问题列表 - type: {}, onlyPriority: {}, page: {}, pageSize: {}", 
                type, onlyPriority, page, pageSize);
        
        CommonProblemQueryDTO queryDTO = new CommonProblemQueryDTO();
        queryDTO.setType(type);
        queryDTO.setOnlyPriority(onlyPriority);
        queryDTO.setPage(page);
        queryDTO.setPageSize(pageSize);
        
        IPage<CommonProblemVO> problemPage = commonProblemsService.getProblemsPage(queryDTO);
        return Result.success(problemPage);
    }

    /**
     * 根据问题ID查询问题详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询问题详情", description = "根据问题ID查询问题的详细信息")
    public Result<CommonProblemVO> getProblemDetail(
            @Parameter(description = "问题ID") @PathVariable Integer id) {
        
        log.info("查询问题详情 - id: {}", id);
        CommonProblemVO problemDetail = commonProblemsService.getProblemDetail(id);
        return Result.success(problemDetail);
    }
}
