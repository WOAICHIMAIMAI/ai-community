package com.zheng.aicommunitybackend.tool;

import com.zheng.aicommunitybackend.service.AddressBookService;
import com.zheng.aicommunitybackend.service.RepairOrdersService;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistration {

    @Resource
    private RepairOrdersService repairOrdersService;

    @Resource
    private AddressBookService addressBookService;

    @Bean
    public ToolCallback[] allTools(){
        RepairTools repairTools = new RepairTools(repairOrdersService);
        DateTimeTools dateTimeTools = new DateTimeTools();
        AddressBookTools addressBookTools = new AddressBookTools(addressBookService);
        return ToolCallbacks.from(
                repairTools,
                dateTimeTools,
                addressBookTools
        );
    }
}
