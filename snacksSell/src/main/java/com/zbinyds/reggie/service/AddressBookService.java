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
     *
     * @param addressBook：封装好的参数信息
     * @return：返回addressBook对象
     */
    AddressBook setDefaultAddress(AddressBook addressBook, HttpSession session);

    /**
     * 查询指定用户的全部地址
     *
     * @param addressBook：封装好的参数信息
     * @return：返回该用户全部地址信息
     */
    List<AddressBook> list(AddressBook addressBook);

    /**
     * 根据用户id查询默认地址
     * @param session ：通过session获取当前登录用户
     * @return ：返回该用户的默认地址
     */
    AddressBook getDefaultAddress(HttpSession session);
}
