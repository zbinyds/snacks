package com.zbinyds.reggie.controller;

import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.pojo.AddressBook;
import com.zbinyds.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author zbinyds
 * @time 2022/08/18 12:01
 * <p>
 * 地址薄管理
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址信息
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook, HttpSession session) {
        addressBook.setUserId((Long) session.getAttribute("user"));
        log.info("addressBook:{}", addressBook);
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 删除地址信息
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> remove(Long ids) {
        addressBookService.removeById(ids);
        return R.success("删除地址成功");
    }

    /**
     * 修改地址信息
     *
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<AddressBook> update(@RequestBody AddressBook addressBook, HttpSession session) {
        addressBook.setUserId((Long) session.getAttribute("user"));
        log.info("addressBook:{}", addressBook);
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 查询当前登录用户的全部地址
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook, HttpSession session) {
        addressBook.setUserId((Long) session.getAttribute("user"));
        log.info("addressBook:{}", addressBook);
        return R.success(addressBookService.list(addressBook));
    }

    /**
     * 设置默认地址。默认地址唯一
     */
    @PutMapping("default")
    @Transactional
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook, HttpSession session) {
        log.info("addressBook:{}", addressBook);
        addressBookService.setDefaultAddress(addressBook,session);
        return R.success(addressBook);
    }

    /**
     * 根据地址id查询地址。用于修改地址时的数据回显
     */
    @GetMapping("/{id}")
    public R<AddressBook> get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        return addressBook != null ? R.success(addressBook) : R.error("该地址不存在");
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")
    public R<AddressBook> getDefault(HttpSession session) {
        AddressBook addressBook = addressBookService.getDefaultAddress(session);
        return addressBook != null ? R.success(addressBook) : R.error("该地址不存在");
    }
}
