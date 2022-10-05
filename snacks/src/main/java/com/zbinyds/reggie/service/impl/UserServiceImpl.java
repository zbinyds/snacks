package com.zbinyds.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.mapper.UserMapper;
import com.zbinyds.reggie.pojo.User;
import com.zbinyds.reggie.service.UserService;
import com.zbinyds.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 前台用户管理-service层
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Boolean sendMsg(User user) {
        // 获取手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)) {
            // 使用工具类ValidateCodeUtils（utils下），生成随机的6位验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            log.info("验证码 {}",code);

            // 使用阿里云提供的短信服务，发送短信
//            SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);

            // 将生成的验证码保存到redis中，设置有效期两分钟。
            stringRedisTemplate.opsForValue().set(phone, code, 2, TimeUnit.MINUTES);
            return true;
        }
        return false;
    }

    @Override
    public R<User> login(Map map, HttpSession session) {
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
            User user = this.getOne(userQueryWrapper);
            // 判断当前用户是否为新用户，如果是新用户就自动完成注册
            if (user == null) {
                User u = new User();
                u.setPhone(phone);
                this.save(u);
            }
            // 再次查询该用户是否为新用户
            user = this.getOne(userQueryWrapper);
            // 将登陆成功的用户id存入session，拦截器获取到id，就会进行放行。
            session.setAttribute("user", user.getId());

            // 用户登录成功后，将redis中的验证码进行删除
            stringRedisTemplate.delete(phone);
            return R.success(user);
        }
        // 登录失败
        return R.error("验证码输入错误");
    }
}




