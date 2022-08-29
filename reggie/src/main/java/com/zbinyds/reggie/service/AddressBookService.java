package com.zbinyds.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbinyds.reggie.pojo.AddressBook;

import javax.servlet.http.HttpSession;
import java.util.List;


/**
 * 地址薄管理-service层
 */
public interface AddressBookService extends IService<AddressBook> {

    /**
     * 设置用户默认地址
     * @param addressBook：封装好的参数信息
     * @return：返回addressBook对象
     */
    AddressBook defaultAddress(AddressBook addressBook);

    /**
     * 查询指定用户的全部地址
     * @param addressBook：封装好的参数信息
     * @return：返回该用户全部地址信息
     */
    List<AddressBook> list(AddressBook addressBook);

    AddressBook getDefaultAddress(HttpSession session);
}
