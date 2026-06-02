package com.market.online.util;

import com.market.online.enums.BizCodeEnum;
import com.market.online.interceptor.LoginInterceptor;
import com.market.online.vo.LoginUser;

public class AuthUtil {

    private AuthUtil() {
    }

    public static LoginUser currentUser() {
        return LoginInterceptor.threadLocal.get();
    }

    public static boolean isAdmin() {
        LoginUser loginUser = currentUser();
        return loginUser != null && Integer.valueOf(1).equals(loginUser.getAdmin());
    }

    public static JsonData requireAdmin() {
        return isAdmin() ? null : JsonData.buildResult(BizCodeEnum.PERMISSION_DENIED);
    }
}
