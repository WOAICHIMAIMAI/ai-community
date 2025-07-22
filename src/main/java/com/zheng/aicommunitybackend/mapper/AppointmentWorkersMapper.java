package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zheng.aicommunitybackend.domain.entity.AppointmentWorkers;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约服务人员Mapper
 * @author ZhengJJ
 * @description 针对表【appointment_workers(预约服务人员表)】的数据库操作Mapper
 * @createDate 2025-01-22 10:00:00
 */
@Mapper
public interface AppointmentWorkersMapper extends BaseMapper<AppointmentWorkers> {

}
