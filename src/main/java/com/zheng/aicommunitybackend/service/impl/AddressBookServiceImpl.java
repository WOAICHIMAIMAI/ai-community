package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.AddressBookDTO;
import com.zheng.aicommunitybackend.domain.entity.AddressBook;
import com.zheng.aicommunitybackend.exception.BaseException;
import com.zheng.aicommunitybackend.service.AddressBookService;
import com.zheng.aicommunitybackend.mapper.AddressBookMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
* @author ZhengJJ
* @description 针对表【address_book(用户地址簿)】的数据库操作Service实现
* @createDate 2025-07-17 10:06:19
*/
@Service
@Slf4j
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook>
    implements AddressBookService {

    /**
     * 添加地址
     *
     * @param addressBookDTO 地址信息DTO
     * @return 添加结果
     */
    @Override
    public boolean addAddress(AddressBookDTO addressBookDTO) {
        // 将DTO转换为实体
        AddressBook addressBook = new AddressBook();
        BeanUtils.copyProperties(addressBookDTO, addressBook);
        
        // 设置用户ID
        Long userId = UserContext.getUserId();
        addressBook.setUserId(userId);
        
        // 设置创建和更新时间
        Date now = new Date();
        addressBook.setCreateTime(now);
        addressBook.setUpdateTime(now);
        
        // 如果是默认地址，先将其他地址设为非默认
        if (addressBook.getIsDefault() != null && addressBook.getIsDefault() == 1) {
            resetDefaultAddress(userId);
        }
        
        return this.save(addressBook);
    }

    /**
     * 更新地址
     *
     * @param id 地址ID
     * @param addressBookDTO 地址信息DTO
     * @return 更新结果
     */
    @Override
    @Transactional
    public boolean updateAddress(Long id, AddressBookDTO addressBookDTO) {
        // 检查地址是否属于当前用户
        getAddressAndCheckOwnership(id);
        
        // 将DTO转换为实体
        AddressBook addressBook = new AddressBook();
        BeanUtils.copyProperties(addressBookDTO, addressBook);
        addressBook.setId(id);
        
        // 设置更新时间
        addressBook.setUpdateTime(new Date());
        
        // 如果是默认地址，先将其他地址设为非默认
        if (addressBook.getIsDefault() != null && addressBook.getIsDefault() == 1) {
            resetDefaultAddress(UserContext.getUserId());
        }
        
        return this.updateById(addressBook);
    }

    /**
     * 删除地址
     *
     * @param id 地址ID
     * @return 删除结果
     */
    @Override
    public boolean deleteAddress(Long id) {
        // 检查地址是否属于当前用户
        getAddressAndCheckOwnership(id);
        
        return this.removeById(id);
    }

    /**
     * 查询用户的所有地址
     *
     * @return 地址列表
     */
    @Override
    public List<AddressBook> listUserAddresses() {
        Long userId = UserContext.getUserId();
        
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        queryWrapper.orderByDesc(AddressBook::getIsDefault); // 默认地址排在前面
        queryWrapper.orderByDesc(AddressBook::getUpdateTime); // 按更新时间降序
        
        return this.list(queryWrapper);
    }

    /**
     * 获取地址详情
     *
     * @param id 地址ID
     * @return 地址信息
     */
    @Override
    public AddressBook getAddressDetail(Long id) {
        return getAddressAndCheckOwnership(id);
    }

    /**
     * 设置默认地址
     *
     * @param id 地址ID
     * @return 设置结果
     */
    @Override
    @Transactional
    public boolean setDefaultAddress(Long id) {
        // 检查地址是否属于当前用户
        getAddressAndCheckOwnership(id);
        
        Long userId = UserContext.getUserId();
        
        // 先将所有地址设为非默认
        resetDefaultAddress(userId);
        
        // 将指定地址设为默认
        AddressBook defaultAddress = new AddressBook();
        defaultAddress.setId(id);
        defaultAddress.setIsDefault(1);
        defaultAddress.setUpdateTime(new Date());
        
        return this.updateById(defaultAddress);
    }

    /**
     * 获取用户默认地址
     *
     * @return 默认地址信息，如果没有则返回null
     */
    @Override
    public AddressBook getDefaultAddress() {
        Long userId = UserContext.getUserId();
        
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        queryWrapper.eq(AddressBook::getIsDefault, 1);
        
        return this.getOne(queryWrapper);
    }
    
    /**
     * 重置用户的所有地址为非默认
     *
     * @param userId 用户ID
     */
    private void resetDefaultAddress(Long userId) {
        // 创建更新条件
        LambdaQueryWrapper<AddressBook> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(AddressBook::getUserId, userId);
        updateWrapper.eq(AddressBook::getIsDefault, 1);
        
        // 创建要更新的对象
        AddressBook addressBook = new AddressBook();
        addressBook.setIsDefault(0);
        addressBook.setUpdateTime(new Date());
        
        // 更新所有符合条件的记录
        this.update(addressBook, updateWrapper);
    }
    
    /**
     * 获取地址并检查所有权
     *
     * @param id 地址ID
     * @return 地址信息
     * @throws BaseException 如果地址不存在或不属于当前用户
     */
    private AddressBook getAddressAndCheckOwnership(Long id) {
        AddressBook addressBook = this.getById(id);
        if (addressBook == null) {
            log.error("地址不存在, id: {}", id);
            throw new BaseException("地址不存在");
        }
        
        Long userId = UserContext.getUserId();
        if (!addressBook.getUserId().equals(userId)) {
            log.error("地址不属于当前用户, addressId: {}, userId: {}", id, userId);
            throw new BaseException("没有操作权限");
        }
        
        return addressBook;
    }
}




