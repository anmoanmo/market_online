package com.buka.service;

import com.buka.dto.UserLoginDto;
import com.buka.dto.UserRegDto;
import com.buka.model.UserDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.buka.util.JsonData;
import com.buka.vo.LoginUser;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zyd
 * @since 2025-02-09
 */
public interface UserService extends IService<UserDO> {

    String upload(MultipartFile file);

    JsonData register(UserRegDto userRegDto);

    JsonData Login(UserLoginDto userLoginDto);

    JsonData info();

    JsonData adminList(int page, int size);
    JsonData adminUpdatePwd(Long userId, String newPwd);
    JsonData updateInfo(LoginUser updateData);
}
