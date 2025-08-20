package com.zheng.aicommunitybackend.controller;

import com.zheng.aicommunitybackend.domain.dto.RedPacketActivityCreateDTO;
import com.zheng.aicommunitybackend.domain.dto.RedPacketGrabDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 红包Controller测试 - 验证DTO和VO的使用
 */
@SpringBootTest
public class RedPacketControllerTest {

    @Test
    public void testRedPacketActivityCreateDTO() {
        // 测试创建活动DTO
        RedPacketActivityCreateDTO createDTO = new RedPacketActivityCreateDTO();
        createDTO.setActivityName("测试活动");
        createDTO.setActivityDesc("测试描述");
        createDTO.setTotalAmount(100.0);
        createDTO.setTotalCount(10);
        createDTO.setStartTime(LocalDateTime.now());
        createDTO.setEndTime(LocalDateTime.now().plusMinutes(3600000));
        createDTO.setAlgorithm("DOUBLE_AVERAGE");
        createDTO.setMinAmount(1L);
        createDTO.setStartImmediately(false);

        // 验证DTO字段
        assertEquals("测试活动", createDTO.getActivityName());
        assertEquals("测试描述", createDTO.getActivityDesc());
        assertEquals(Double.valueOf(100.0), createDTO.getTotalAmount());
        assertEquals(Integer.valueOf(10), createDTO.getTotalCount());
        assertEquals("DOUBLE_AVERAGE", createDTO.getAlgorithm());
        assertEquals(Long.valueOf(1), createDTO.getMinAmount());
        assertEquals(Boolean.FALSE, createDTO.getStartImmediately());

        // 测试金额转换
        assertEquals(Long.valueOf(10000), createDTO.getTotalAmountCents());
    }

    @Test
    public void testRedPacketGrabDTO() {
        // 测试抢红包DTO
        RedPacketGrabDTO grabDTO = new RedPacketGrabDTO();
        grabDTO.setActivityId(123L);

        // 验证DTO字段
        assertEquals(Long.valueOf(123), grabDTO.getActivityId());
    }

    @Test
    public void testDTOValidation() {
        // 测试DTO校验
        RedPacketActivityCreateDTO createDTO = new RedPacketActivityCreateDTO();
        
        // 测试校验方法
        createDTO.setStartTime(LocalDateTime.now());
        createDTO.setEndTime(LocalDateTime.now().plusMinutes(3600000));
        createDTO.setTotalAmount(100.0);
        createDTO.setTotalCount(10);
        createDTO.setMinAmount(1L);
        createDTO.setMaxAmount(5000L);

        // 验证校验方法
        assertTrue(createDTO.isEndTimeAfterStartTime());
        assertTrue(createDTO.isValidDuration());
        assertTrue(createDTO.isValidAverageAmount());
        assertTrue(createDTO.isMaxAmountValid());
    }

    @Test
    public void testDTOValidationFailure() {
        // 测试校验失败的情况
        RedPacketActivityCreateDTO createDTO = new RedPacketActivityCreateDTO();
        
        // 结束时间早于开始时间
        createDTO.setStartTime(LocalDateTime.now());
        createDTO.setEndTime(LocalDateTime.now().plusMinutes(3600000));
        assertFalse(createDTO.isEndTimeAfterStartTime());

        // 平均金额小于1分
        createDTO.setTotalAmount(0.05); // 5分
        createDTO.setTotalCount(10); // 10个红包，平均0.5分
        assertFalse(createDTO.isValidAverageAmount());

        // 最大金额小于最小金额
        createDTO.setMinAmount(100L);
        createDTO.setMaxAmount(50L);
        assertFalse(createDTO.isMaxAmountValid());
    }
}
