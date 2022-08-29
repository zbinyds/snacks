package com.zbinyds.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbinyds.reggie.commen.BaseContext;
import com.zbinyds.reggie.mapper.AddressBookMapper;
import com.zbinyds.reggie.pojo.AddressBook;
import com.zbinyds.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 地址薄管理-service层
 */
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    @Transactional // 涉及到多张表操作，该方法需要加上事务
    public AddressBook defaultAddress(AddressBook addressBook) {
        // 先将该用户所有地址全部置为普通地址
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        wrapper.set(AddressBook::getIsDefault, 0);
        //SQL:update address_book set is_default = 0 where user_id = ?
        this.update(wrapper);

        // 再将此地址设置为默认地址
        addressBook.setIsDefault(1);
        //SQL:update address_book set is_default = 1 where id = ?
        this.updateById(addressBook);
        return addressBook;
    }

    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getIsDefault);
        return this.list(queryWrapper);
    }

    @Override
    public AddressBook getDefaultAddress(HttpSession session) {
        // 条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, session.getAttribute("user"));
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        return getOne(queryWrapper);
    }
}
