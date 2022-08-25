package com.zbinyds.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.pojo.User;
import com.zbinyds.reggie.service.UserService;
import com.zbinyds.reggie.utils.SMSUtils;
import com.zbinyds.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zbinyds
 * @time 2022/08/19 23:32
 * <p>
 * 用户登录
 */

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成手机短信验证码
     *
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        // 获取手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)) {
            // 生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            log.info(code);

            // 使用阿里云提供的短信服务，发送短信
//            SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);

            /*// 将验证码保存到session域中
            session.setAttribute(phone, code);*/
            // 将生成的验证码保存到redis中，设置有效期两分钟。
            stringRedisTemplate.opsForValue().set(phone, code, 2, TimeUnit.MINUTES);

            return R.success("手机验证码短信发送成功");
        }

        return R.error("短信发送失败");
    }

    /**
     * 前台用户登录
     *
     * @param map
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        // 获取手机号
        String phone = (String) map.get("phone");

        // 获取验证码
        String code = (String) map.get("code");

        // 从redis中获取验证码
        String redis_code = stringRedisTemplate.opsForValue().get(phone);

        // 进行验证码比对
        if (StringUtils.isNotEmpty(phone) && code.equals(redis_code)) {
            // 获取当前登录用户
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("phone", phone);
            User user = userService.getOne(userQueryWrapper);
            // 判断当前用户是否为新用户，如果是新用户就自动完成注册
            if (user == null) {
                User u = new User();
                u.setPhone(phone);
                userService.save(u);
            }
            // 再次查询该用户是否为新用户
            user = userService.getOne(userQueryWrapper);
            // 将登陆成功的用户id存入session，拦截器获取到id，就会进行放行。
            session.setAttribute("user", user.getId());

            // 用户登录成功后，将redis中的验证码进行删除
            stringRedisTemplate.delete(phone);
            return R.success(user);
        }
        // 登录失败
        return R.error("验证码输入错误");
    }

    /**
     * 用户-退出登录
     *
     * @param session
     */
    @PostMapping("/loginout")
    public R<String> loginOut(HttpSession session) {
        session.removeAttribute("user");
        return R.success("退出登录成功");
    }
}
