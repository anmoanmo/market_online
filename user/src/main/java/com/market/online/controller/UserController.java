package com.market.online.controller;


import com.market.online.dto.UserLoginDto;
import com.market.online.dto.UserRegDto;
import com.market.online.vo.LoginUser;
import com.market.online.enums.BizCodeEnum;
import com.market.online.service.UserService;
import com.market.online.util.AuthUtil;
import com.market.online.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author LZX
 * @since 2025-02-09
 */
@RestController
@RequestMapping("/api/user/v1")
public class UserController {
    @Autowired
    private UserService userService;
    /**
     * @description:上传头像
     * @author: LZX
     * @date: 2025/2/13 12:37
     * @param: [file]
     * @return: 返回外连接
     **/
    @PostMapping("/upload")
    public JsonData upload(@RequestPart("file") MultipartFile file) {
      String path =  userService.upload(file);
        return path !=null ? JsonData.buildSuccess(path) : JsonData.buildResult(BizCodeEnum.UPLOAD_ERROR);
    }
/**
 * @description:实现注册功能
 * @author: LZX
 * @date: 2025/2/13 17:13
 * @param: [userRegDto]
 * @return: com.market.online.util.JsonData
 **/
    @PostMapping("/register")
    public JsonData register(@RequestBody UserRegDto userRegDto) {
        return userService.register(userRegDto);
    }

    @PostMapping("/login")
    public JsonData Login(@RequestBody UserLoginDto userLoginDto) {
        return userService.Login(userLoginDto);
    }

    @GetMapping("/info")
    public JsonData info() {
        return userService.info();
    }

    @PutMapping("/update_info")
    public JsonData updateInfo(@RequestBody com.market.online.vo.LoginUser updateData) {
        return userService.updateInfo(updateData);
    }

    @GetMapping("/admin_list")
    public JsonData adminList(@RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        return userService.adminList(page, size);
    }

    @GetMapping("/list_by_register_date")
    public JsonData listByRegisterDate(@RequestParam("date") String date) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        return userService.listByRegisterDate(date);
    }

    @PutMapping("/admin_update_pwd")
    public JsonData adminUpdatePwd(@RequestParam("userId") Long userId, @RequestParam("newPwd") String newPwd) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        return userService.adminUpdatePwd(userId, newPwd);
    }
}

