package com.zbinyds.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbinyds.reggie.pojo.AddressBook;


public interface AddressBookService extends IService<AddressBook> {

    /**
     * 设置用户默认地址
     * @param addressBook：封装好的参数信息
     * @return：返回addressBook对象
     */
    AddressBook defaultAddress(AddressBook addressBook);
}
