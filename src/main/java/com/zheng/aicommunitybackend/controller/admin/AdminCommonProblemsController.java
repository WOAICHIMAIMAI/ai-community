package com.zheng.aicommunitybackend.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zheng.aicommunitybackend.common.Result;
import com.zheng.aicommunitybackend.domain.dto.CommonProblemCategoryDTO;
import com.zheng.aicommunitybackend.domain.dto.CommonProblemQueryDTO;
import com.zheng.aicommunitybackend.domain.entity.CommonProblems;
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
 * 管理员端常见问题接口
 */
@RestController
@RequestMapping("/admin/common-problems")
@Slf4j
@Tag(name = "管理员常见问题接口", description = "管理员常见问题管理接口")
public class AdminCommonProblemsController {

    @Autowired
    private CommonProblemsService commonProblemsService;

    /**
     * 获取问题分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取问题分类列表", description = "获取所有问题分类及每个分类下的问题数量")
    public Result<List<CommonProblemCategoryDTO>> getProblemCategories() {
        log.info("管理员获取问题分类列表");
        List<CommonProblemCategoryDTO> categories = commonProblemsService.getProblemCategories();
        return Result.success(categories);
    }

    /**
     * 根据条件查询问题列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询问题列表", description = "根据分类、优先级和关键词查询问题列表")
    public Result<IPage<CommonProblemVO>> getProblemsPage(
            @Parameter(description = "问题分类类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "是否只查询置顶问题") @RequestParam(required = false) Boolean onlyPriority,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("管理员查询问题列表 - type: {}, onlyPriority: {}, keyword: {}, page: {}, pageSize: {}",
                type, onlyPriority, keyword, page, pageSize);

        CommonProblemQueryDTO queryDTO = new CommonProblemQueryDTO();
        queryDTO.setType(type);
        queryDTO.setOnlyPriority(onlyPriority);
        queryDTO.setKeyword(keyword);
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
        
        log.info("管理员查询问题详情 - id: {}", id);
        CommonProblemVO problemDetail = commonProblemsService.getProblemDetail(id);
        return Result.success(problemDetail);
    }

    /**
     * 新增问题
     */
    @PostMapping
    @Operation(summary = "新增问题", description = "新增常见问题")
    public Result<Boolean> addProblem(@RequestBody CommonProblems problem) {
        log.info("管理员新增问题: {}", problem.getProblem());
        boolean result = commonProblemsService.save(problem);
        return Result.success(result);
    }

    /**
     * 更新问题
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新问题", description = "更新常见问题")
    public Result<Boolean> updateProblem(
            @Parameter(description = "问题ID") @PathVariable Integer id,
            @RequestBody CommonProblems problem) {
        
        log.info("管理员更新问题 - id: {}, problem: {}", id, problem.getProblem());
        problem.setId(id);
        boolean result = commonProblemsService.updateById(problem);
        return Result.success(result);
    }

    /**
     * 删除问题
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除问题", description = "删除常见问题")
    public Result<Boolean> deleteProblem(
            @Parameter(description = "问题ID") @PathVariable Integer id) {

        log.info("管理员删除问题 - id: {}", id);
        boolean result = commonProblemsService.removeById(id);
        return Result.success(result);
    }

    /**
     * 批量删除问题
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除问题", description = "批量删除常见问题")
    public Result<Boolean> batchDeleteProblems(@RequestBody BatchDeleteRequest request) {
        log.info("管理员批量删除问题 - ids: {}", request.getIds());
        boolean result = commonProblemsService.removeByIds(request.getIds());
        return Result.success(result);
    }

    /**
     * 设置问题优先级
     */
    @PutMapping("/{id}/priority")
    @Operation(summary = "设置问题优先级", description = "设置问题的优先级（0-普通，1-置顶）")
    public Result<Boolean> setProblemPriority(
            @Parameter(description = "问题ID") @PathVariable Integer id,
            @RequestBody PriorityRequest request) {

        log.info("管理员设置问题优先级 - id: {}, priority: {}", id, request.getPriority());
        CommonProblems problem = new CommonProblems();
        problem.setId(id);
        problem.setPriority(request.getPriority());
        boolean result = commonProblemsService.updateById(problem);
        return Result.success(result);
    }

    /**
     * 批量删除请求
     */
    public static class BatchDeleteRequest {
        private java.util.List<Integer> ids;

        public java.util.List<Integer> getIds() {
            return ids;
        }

        public void setIds(java.util.List<Integer> ids) {
            this.ids = ids;
        }
    }

    /**
     * 优先级设置请求
     */
    public static class PriorityRequest {
        private Integer priority;

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }
    }
}
