package com.buka.interceptor;

import com.buka.enums.BizCodeEnum;
import com.buka.util.CommonUtil;
import com.buka.util.JWTUtil;
import com.buka.util.JsonData;
import com.buka.vo.LoginUser;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @className: LoginInterceptor
 * @author: LZX
 * @date: 2025/2/14 11:26
 * @Version: 1.0
 * @description:
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if (token == null) {
            token = request.getHeader("Authorization");
        }
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token != null) {
            Claims claims = JWTUtil.checkJWT(token);
            if (claims != null) {
                LoginUser loginUser = new LoginUser();
                Object idObj = claims.get("id");
                if (idObj instanceof Integer) {
                    loginUser.setId(((Integer) idObj).longValue());
                } else if (idObj instanceof Long) {
                    loginUser.setId((Long) idObj);
                }
                loginUser.setName(claims.get("name", String.class));
                loginUser.setHeadImg(claims.get("headImg", String.class));
                loginUser.setMail(claims.get("mail", String.class));
                Object adminObj = claims.get("admin");
                if (adminObj instanceof Number) {
                    loginUser.setAdmin(((Number) adminObj).intValue());
                }
                threadLocal.set(loginUser);
                return true;
            }
        }
        CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.Login_ERROR));
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        threadLocal.remove();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

}


