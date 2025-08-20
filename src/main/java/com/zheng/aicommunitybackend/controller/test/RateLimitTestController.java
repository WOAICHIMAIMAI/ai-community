package com.zheng.aicommunitybackend.controller.test;

import com.zheng.aicommunitybackend.annotation.RateLimit;
import com.zheng.aicommunitybackend.domain.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 限流测试Controller
 */
@RestController
@RequestMapping("/test/rate-limit")
@Tag(name = "限流测试", description = "测试限流功能")
@Slf4j
public class RateLimitTestController {

    @GetMapping("/simple")
    @RateLimit(
        key = "test_simple",
        limitType = RateLimit.LimitType.GLOBAL,
        windowSize = 10,
        maxRequests = 5,
        algorithm = RateLimit.Algorithm.FIXED_WINDOW,
        message = "测试限流：10秒内最多5次请求"
    )
    @Operation(summary = "简单限流测试", description = "10秒内最多5次请求")
    public Result<String> simpleTest() {
        log.info("简单限流测试被调用");
        return Result.success("简单限流测试成功，当前时间：" + System.currentTimeMillis());
    }

    @GetMapping("/user")
    @RateLimit(
        key = "test_user",
        limitType = RateLimit.LimitType.USER,
        windowSize = 5,
        maxRequests = 3,
        algorithm = RateLimit.Algorithm.SLIDING_WINDOW,
        message = "用户限流：5秒内最多3次请求"
    )
    @Operation(summary = "用户级限流测试", description = "5秒内最多3次请求")
    public Result<String> userTest() {
        log.info("用户级限流测试被调用");
        return Result.success("用户级限流测试成功，当前时间：" + System.currentTimeMillis());
    }

    @GetMapping("/multiple")
    @RateLimit(
        key = "test_multiple_user",
        limitType = RateLimit.LimitType.USER,
        windowSize = 1,
        maxRequests = 1,
        algorithm = RateLimit.Algorithm.FIXED_WINDOW,
        message = "用户限流：每秒最多1次"
    )
    @RateLimit(
        key = "test_multiple_global",
        limitType = RateLimit.LimitType.GLOBAL,
        windowSize = 1,
        maxRequests = 10,
        algorithm = RateLimit.Algorithm.FIXED_WINDOW,
        message = "全局限流：每秒最多10次"
    )
    @Operation(summary = "多重限流测试", description = "同时应用用户级和全局限流")
    public Result<String> multipleTest() {
        log.info("多重限流测试被调用");
        return Result.success("多重限流测试成功，当前时间：" + System.currentTimeMillis());
    }

    @GetMapping("/no-limit")
    @Operation(summary = "无限流测试", description = "没有限流的接口")
    public Result<String> noLimitTest() {
        log.info("无限流测试被调用");
        return Result.success("无限流测试成功，当前时间：" + System.currentTimeMillis());
    }
}
