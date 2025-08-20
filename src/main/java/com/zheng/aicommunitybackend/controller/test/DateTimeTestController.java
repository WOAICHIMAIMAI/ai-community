package com.zheng.aicommunitybackend.controller.test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zheng.aicommunitybackend.domain.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 日期时间测试Controller
 */
@RestController
@RequestMapping("/test/datetime")
@Tag(name = "日期时间测试", description = "测试日期时间格式处理")
@Slf4j
public class DateTimeTestController {

    @Data
    public static class DateTimeTestDTO {
        @NotNull(message = "开始时间不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        @NotNull(message = "结束时间不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;

        private String description;
    }

    @Data
    public static class DateTimeTestVO {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String description;
        private String message;
    }

    @PostMapping("/test")
    @Operation(summary = "测试日期时间格式", description = "测试LocalDateTime的序列化和反序列化")
    public Result<DateTimeTestVO> testDateTime(@Valid @RequestBody DateTimeTestDTO testDTO) {
        log.info("收到日期时间测试请求: {}", testDTO);
        
        DateTimeTestVO result = new DateTimeTestVO();
        result.setStartTime(testDTO.getStartTime());
        result.setEndTime(testDTO.getEndTime());
        result.setDescription(testDTO.getDescription());
        result.setMessage("日期时间格式测试成功");
        
        log.info("返回日期时间测试结果: {}", result);
        return Result.success(result);
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前时间", description = "返回当前时间的LocalDateTime格式")
    public Result<DateTimeTestVO> getCurrentTime() {
        DateTimeTestVO result = new DateTimeTestVO();
        result.setStartTime(LocalDateTime.now());
        result.setEndTime(LocalDateTime.now().plusHours(1));
        result.setDescription("当前时间测试");
        result.setMessage("获取当前时间成功");
        
        return Result.success(result);
    }
}
