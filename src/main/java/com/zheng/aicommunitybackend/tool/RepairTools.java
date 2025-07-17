package com.zheng.aicommunitybackend.tool;


import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.RepairOrderCreateDTO;
import com.zheng.aicommunitybackend.service.RepairOrdersService;
import com.zheng.aicommunitybackend.service.impl.RepairOrdersServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 报修相关工具
 */
public class RepairTools {

    private final RepairOrdersService repairOrdersService;
    public RepairTools(RepairOrdersService repairOrdersService){
        this.repairOrdersService = repairOrdersService;
    }

    /**
     * 创建报修工单
     * @param repairOrderCreateDTO 报修工单参数
     * @return AI提示词
     */
    @Tool(description = "帮助用户创建报修工单")
    public String repairOrderCreate(@ToolParam(description = "创建工单所需要的参数") RepairOrderCreateDTO repairOrderCreateDTO) {
        String orderNumber = repairOrdersService.createRepairOrder(repairOrderCreateDTO, UserContext.getUserId());
        return "已成功创建报修工单，工单ID" + orderNumber;
    }
}
