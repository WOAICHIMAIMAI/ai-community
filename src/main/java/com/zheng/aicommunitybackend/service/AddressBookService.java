package com.zheng.aicommunitybackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.dto.AddressBookDTO;
import com.zheng.aicommunitybackend.domain.entity.AddressBook;

import java.util.List;

/**
* @author ZhengJJ
* @description 针对表【address_book(用户地址簿)】的数据库操作Service
* @createDate 2025-07-17 10:06:19
*/
public interface AddressBookService extends IService<AddressBook> {
    
    /**
     * 添加地址
     * @param addressBookDTO 地址信息DTO
     * @return 添加结果
     */
    boolean addAddress(AddressBookDTO addressBookDTO);
    
    /**
     * 更新地址
     * @param id 地址ID
     * @param addressBookDTO 地址信息DTO
     * @return 更新结果
     */
    boolean updateAddress(Long id, AddressBookDTO addressBookDTO);
    
    /**
     * 删除地址
     * @param id 地址ID
     * @return 删除结果
     */
    boolean deleteAddress(Long id);
    
    /**
     * 查询用户的所有地址
     * @return 地址列表
     */
    List<AddressBook> listUserAddresses();
    
    /**
     * 获取地址详情
     * @param id 地址ID
     * @return 地址信息
     */
    AddressBook getAddressDetail(Long id);
    
    /**
     * 设置默认地址
     * @param id 地址ID
     * @return 设置结果
     */
    boolean setDefaultAddress(Long id);
    
    /**
     * 获取用户默认地址
     * @return 默认地址信息，如果没有则返回null
     */
    AddressBook getDefaultAddress();
}
