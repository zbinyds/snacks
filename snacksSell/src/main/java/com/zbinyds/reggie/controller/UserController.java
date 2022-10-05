package com.zbinyds.reggie.controller;

import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.pojo.User;
import com.zbinyds.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author zbinyds
 * @time 2022/08/19 23:32
 * <p>
 * 前台用户管理
 */

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 生成手机短信验证码
     *
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user) {
        Boolean flag = userService.sendMsg(user); // true：表示发送成功；false：表示发送失败。
        return flag ? R.success("手机验证码短信发送成功") : R.error("短信发送失败");
    }

    /**
     * 前台用户登录
     *
     * @param map：使用map类型来接收传递来的参数信息
     * @return：登陆成功返回登录对象，否则返回错误提示信息
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        return userService.login(map,session);
    }

    /**
     * 前台用户-退出登录
     *
     * @param session：获取正在登录的用户id
     * @return ：返回提示信息
     */
    @PostMapping("/loginout")
    public R<String> loginOut(HttpSession session) {
        session.removeAttribute("user");
        return R.success("退出登录成功");
    }
}
