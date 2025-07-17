package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.domain.dto.AddressBookDTO;
import com.zheng.aicommunitybackend.domain.entity.AddressBook;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.service.AddressBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户地址簿控制器
 */
@RestController
@RequestMapping("/user/addressBook")
@Tag(name = "用户地址簿接口", description = "用户地址簿相关接口")
@Slf4j
public class UserAddressBookController {

    private final AddressBookService addressBookService;

    @Autowired
    public UserAddressBookController(AddressBookService addressBookService) {
        this.addressBookService = addressBookService;
    }

    /**
     * 添加地址
     *
     * @param addressBookDTO 地址信息DTO
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加地址", description = "添加用户收货地址")
    public Result<Void> addAddress(@RequestBody @Valid AddressBookDTO addressBookDTO) {
        log.info("用户添加地址：{}", addressBookDTO);
        boolean success = addressBookService.addAddress(addressBookDTO);
        return success ? Result.success() : Result.error("添加地址失败");
    }

    /**
     * 修改地址
     *
     * @param id 地址ID
     * @param addressBookDTO 地址信息DTO
     * @return 修改结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改地址", description = "修改用户收货地址信息")
    public Result<Void> updateAddress(
            @PathVariable("id") @Parameter(description = "地址ID") Long id,
            @RequestBody @Valid AddressBookDTO addressBookDTO) {
        log.info("用户修改地址，id：{}，地址信息：{}", id, addressBookDTO);
        boolean success = addressBookService.updateAddress(id, addressBookDTO);
        return success ? Result.success() : Result.error("修改地址失败");
    }

    /**
     * 删除地址
     *
     * @param id 地址ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址", description = "删除用户收货地址")
    public Result<Void> deleteAddress(@PathVariable("id") @Parameter(description = "地址ID") Long id) {
        log.info("用户删除地址，id：{}", id);
        boolean success = addressBookService.deleteAddress(id);
        return success ? Result.success() : Result.error("删除地址失败");
    }

    /**
     * 查询用户的所有地址
     *
     * @return 地址列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询地址列表", description = "获取当前用户的所有收货地址")
    public Result<List<AddressBook>> listUserAddresses() {
        log.info("查询用户地址列表");
        List<AddressBook> addressList = addressBookService.listUserAddresses();
        return Result.success(addressList);
    }

    /**
     * 获取地址详情
     *
     * @param id 地址ID
     * @return 地址信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取地址详情", description = "根据ID获取地址详细信息")
    public Result<AddressBook> getAddressDetail(@PathVariable("id") @Parameter(description = "地址ID") Long id) {
        log.info("查询地址详情，id：{}", id);
        AddressBook addressBook = addressBookService.getAddressDetail(id);
        return Result.success(addressBook);
    }

    /**
     * 设置默认地址
     *
     * @param id 地址ID
     * @return 设置结果
     */
    @PutMapping("/{id}/default")
    @Operation(summary = "设置默认地址", description = "将指定地址设为默认收货地址")
    public Result<Void> setDefaultAddress(@PathVariable("id") @Parameter(description = "地址ID") Long id) {
        log.info("设置默认地址，id：{}", id);
        boolean success = addressBookService.setDefaultAddress(id);
        return success ? Result.success() : Result.error("设置默认地址失败");
    }

    /**
     * 获取用户默认地址
     *
     * @return 默认地址信息
     */
    @GetMapping("/default")
    @Operation(summary = "获取默认地址", description = "获取用户的默认收货地址")
    public Result<AddressBook> getDefaultAddress() {
        log.info("获取用户默认地址");
        AddressBook defaultAddress = addressBookService.getDefaultAddress();
        return Result.success(defaultAddress);
    }
} 