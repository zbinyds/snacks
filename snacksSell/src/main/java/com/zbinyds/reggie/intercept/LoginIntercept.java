package com.zbinyds.reggie.intercept;

import com.alibaba.fastjson.JSON;
import com.zbinyds.reggie.commen.BaseContext;
import com.zbinyds.reggie.commen.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zbinyds
 * @time 2022/08/16 12:19
 * <p>
 * 登录状态拦截器，未登录的用户不能访问项目。
 */
@Slf4j
public class LoginIntercept implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long currentEmpId = (Long) request.getSession().getAttribute("currentEmp");
        Long user = (Long) request.getSession().getAttribute("user");

        /**
         * 这里存在一个问题？当后台管理员用户登录成功之后再访问前台用户界面，拦截器不会进行拦截
         */
        if (currentEmpId != null) {
            // 将当前登录的后台管理人员id存入ThreadLocal中。
            // 因为我们每次请求都是同一个线程进行处理，所以此时我们可以在请求的任意时刻获取当前管理员的id。
            BaseContext.setCurrentId(currentEmpId);
            return true;
        }

        if (user != null) {
            // 将当前登录的用户id存入ThreadLocal中。
            // 因为我们每次请求都是同一个线程进行处理，所以此时我们可以在请求的任意时刻获取当前用户的id。
            BaseContext.setCurrentId(user);
            return true;
        }

        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return false;
    }
}
