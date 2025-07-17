package com.zheng.aicommunitybackend.tool;

import cn.hutool.json.JSONUtil;
import com.zheng.aicommunitybackend.domain.entity.AddressBook;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.service.AddressBookService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 用户地址簿工具
 */
@AllArgsConstructor
public class AddressBookTools {

    private final AddressBookService addressBookService;

    /**
     * 获取用户默认地址
     */
    @Tool(description = "获取用户的默认地址")
    public String getDefaultAddress() {
        AddressBook defaultAddress = addressBookService.getDefaultAddress();
        return "成功获取用户默认的地址：" + JSONUtil.toJsonStr(defaultAddress);
    }

    /**
     * 获取用户地址簿所有的地址
     */
    @Tool(description = "获取用户地址簿的所有地址")
    public String getAllAddresses(){
        List<AddressBook> addressBooks = addressBookService.listUserAddresses();
        return "成功获取到用户所有的地址：" + JSONUtil.toJsonStr(addressBooks);
    }
}
