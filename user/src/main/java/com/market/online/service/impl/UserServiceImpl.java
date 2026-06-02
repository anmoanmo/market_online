package com.market.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.market.online.dto.UserLoginDto;
import com.market.online.dto.UserRegDto;
import com.market.online.enums.BizCodeEnum;
import com.market.online.feign.CouponFeignService;
import com.market.online.config.MinioUtils;
import com.market.online.interceptor.LoginInterceptor;
import com.market.online.model.UserDO;
import com.market.online.mapper.UserMapper;

import com.market.online.request.NewUserCouponRequest;
import com.market.online.service.NotifyService;
import com.market.online.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.market.online.util.CommonUtil;
import com.market.online.util.JWTUtil;
import com.market.online.util.JsonData;
import com.market.online.vo.LoginUser;
import com.market.online.vo.UserInfoVo;
import org.apache.commons.codec.digest.Md5Crypt;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lzx
 * @since 2025-02-09
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private NotifyService notifyService;
    @Autowired
    private MinioUtils minioUtils;
    @Value("${app.upload-storage:local}")
    private String uploadStorage;
    @Value("${app.public-upload-base-url:http://localhost:8090/uploads/avatar/}")
    private String publicUploadBaseUrl;
    @Value("${minio.bucket:404-shop}")
    private String minioBucket;

    /**
     * @description:处理文件名称并上传
     * @author: LZX
     * @date: 2025/2/13 12:38
     * @param: [file]
     * @return: java.lang.String
     **/
    @Override
    public String upload(MultipartFile file) {
        try {
            String uuid = CommonUtil.generateUUID();
            String originalFilename = file.getOriginalFilename();
            String suffixName = originalFilename != null && originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = uuid + suffixName;
            if ("minio".equalsIgnoreCase(uploadStorage)) {
                String objectName = "avatar/" + fileName;
                String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
                minioUtils.createBucket(minioBucket);
                minioUtils.setBucketPublicRead(minioBucket);
                minioUtils.putObject(minioBucket, objectName, file.getInputStream(), file.getSize(), contentType);
                return minioUtils.getPublicObjectUrl(minioBucket, objectName);
            }
            String uploadDir = System.getProperty("user.dir") + "/uploads/avatar/";
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            java.io.File dest = new java.io.File(uploadDir + fileName);
            file.transferTo(dest);
            String baseUrl = publicUploadBaseUrl.endsWith("/") ? publicUploadBaseUrl : publicUploadBaseUrl + "/";
            return baseUrl + fileName;
        } catch (Exception e) {
            log.error("上传头像失败", e);
            return null;
        }
    }
    /**
     * @description:
     * @author: LZX
     * @date: 2025/2/15 09:49
     * @param: [userLoginDto]
     * @return: com.market.online.util.JsonData
     **/
    @Override
    public JsonData Login(UserLoginDto userLoginDto) {
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDO::getMail, userLoginDto.getMail());
        UserDO userDO = this.getOne(queryWrapper);
        if (userDO != null) {
            if (userDO.getPwd().equals(Md5Crypt.md5Crypt(userLoginDto.getPwd().getBytes(), userDO.getSecret()))) {
                LoginUser loginUser = new LoginUser();
                BeanUtils.copyProperties(userDO, loginUser);
                String jsonWebToken = JWTUtil.geneJsonWebToken(loginUser);
                return JsonData.buildSuccess(jsonWebToken);
            } else {
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_PWD_ERROR);
            }
        }

        return JsonData.buildResult(BizCodeEnum.ACCOUNT_PWD_ERROR);
    }

    /**
     * @description:注册功能
     * @author: LZX
     * @date: 2025/2/13 16:40
     * @param: [userRegDto]
     * @return: com.market.online.util.JsonData
     **/
    @Override
    public JsonData register(UserRegDto userRegDto) {

        if (StringUtils.isBlank(userRegDto.getMail())) {
            return JsonData.buildResult(BizCodeEnum.Email_ERROR);
        }

        if (StringUtils.isNotBlank(userRegDto.getCode())) {
            boolean checkCode = notifyService.checkCode(userRegDto.getMail(), userRegDto.getCode());
            if (!checkCode) {
                return JsonData.buildResult(BizCodeEnum.CODE_ERROR);
            }
        }
        //检测邮箱是否唯一
        if (checkUnique(userRegDto.getMail())) {
            //密码加密
            UserDO user = new UserDO();
            BeanUtils.copyProperties(userRegDto, user);
            //随机生成盐
            user.setSecret("$1$" + CommonUtil.getStringNumRandom(8));
            //原始密码与盐拼接之后md5加密
            String string = Md5Crypt.md5Crypt(user.getPwd().getBytes(), user.getSecret());
            user.setPwd(string);
            //储存进数据库
            this.save(user);
            //创建接收对象
            NewUserCouponRequest newUserCouponRequest=new NewUserCouponRequest();
            //将注册用户的用户id和昵称传入封装
            newUserCouponRequest.setUserId(user.getId());
            newUserCouponRequest.setName(user.getName());
            //远程调用
            JsonData jsonData= couponFeignService.newUserCoupon(newUserCouponRequest);
            if (jsonData.getCode() != 0) {
                throw new RuntimeException();
            }
            return JsonData.buildSuccess();
        } else {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_REPEAT);
        }


    }

    /**
     * @description:检测数据库是否存在同样的邮箱账号
     * @author: LZX
     * @date: 2025/2/13 16:40
     * @param: [mail]
     * @return: boolean
     **/
    public boolean checkUnique(String mail) {
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<>();
        //查数据库是否有一致邮箱
        queryWrapper.eq(UserDO::getMail, mail);
        int count = this.count(queryWrapper);
        if (count > 0) {
            return false;
        }
        return true;
    }

    /**
     * @description:
     * @author: LZX
     * @date: 2025/2/15 09:16
     * @param: []
     * @return: com.market.online.util.JsonData
     **/
    @Override
    public JsonData adminList(int page, int size) {
        Page<UserDO> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(UserDO::getCreateTime);
        page(pageInfo, wrapper);
        return JsonData.buildSuccess(pageInfo);
    }

    @Override
    public JsonData adminUpdatePwd(Long userId, String newPwd) {
        UserDO user = getById(userId);
        if (user == null) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNREGISTER);
        }
        String secret = "$1$" + CommonUtil.getStringNumRandom(8);
        String encryptedPwd = Md5Crypt.md5Crypt(newPwd.getBytes(), secret);
        user.setSecret(secret);
        user.setPwd(encryptedPwd);
        updateById(user);
        return JsonData.buildSuccess();
    }

    @Override
    public JsonData updateInfo(LoginUser updateData) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        if (loginUser == null) return JsonData.buildResult(BizCodeEnum.Login_ERROR);
        UserDO user = getById(loginUser.getId());
        if (user == null) return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNREGISTER);
        if (updateData.getName() != null) user.setName(updateData.getName());
        if (updateData.getHeadImg() != null) user.setHeadImg(updateData.getHeadImg());
        updateById(user);
        return JsonData.buildSuccess();
    }

    @Override
    public JsonData listByRegisterDate(String date) {
        Date threshold;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            threshold = sdf.parse(date);
        } catch (ParseException e) {
            return JsonData.buildError("日期格式错误，请使用 yyyy-MM-dd");
        }
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(UserDO::getCreateTime, threshold);
        wrapper.eq(UserDO::getAdmin, 0);
        wrapper.orderByDesc(UserDO::getCreateTime);
        List<UserDO> list = list(wrapper);
        return JsonData.buildSuccess(list);
    }

    @Override
    public JsonData info() {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        UserDO user = getById(loginUser.getId());
        if (user != null) {
            UserInfoVo userInfoVo = new UserInfoVo();
            BeanUtils.copyProperties(user, userInfoVo);
            return JsonData.buildSuccess(userInfoVo);
        }
        return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNREGISTER);
    }
}
