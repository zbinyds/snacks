package com.zbinyds.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.pojo.User;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 前台用户管理-service层
 */
public interface UserService extends IService<User> {

    /**
     * 生成手机短信验证码
     *
     * @param user
     * @return
     */
    Boolean sendMsg(User user);

    /**
     * 前台用户登录
     */
    R<User> login(Map map, HttpSession session);
}
