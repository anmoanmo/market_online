package com.buka.service.impl;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.buka.constant.CacheKey;
import com.buka.enums.BizCodeEnum;
import com.buka.service.MailService;
import com.buka.service.NotifyService;
import com.buka.util.CheckUtil;
import com.buka.util.CommonUtil;
import com.buka.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @className: NotifyServiceImpl
 * @author: LZX
 * @date: 2025/2/10 15:56
 * @Version: 1.0
 * @description:
 */
@Service
public class NotifyServiceImpl implements NotifyService {
    /**
     * 验证码的标题
     */
    private static final String SUBJECT= "404-Shop 邮箱验证码";

    /**
     * 验证码的内容
     */
    private static final String CONTENT= "您的验证码是%s，有效时间为10分钟。";

    /**
     * 验证码10分钟有效
     */
    private static final int CODE_EXPIRED = 60 * 1000 * 10;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private MailService mailService;
    @Override
    /**
     * @description:判断邮箱合规发送验证码并且防盗刷
     * @author: LZX
     * @date: 2025/2/10 17:28
     * @param: [to]
     * @return: com.buka.util.JsonData
     **/
    public JsonData sendCode(String to) {
        //判断验证码发送时间是否过短
        String key = String.format(CacheKey.CHECK_CODE_KEY, to);
        String code = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(code)) {
           long OldTime = Long.parseLong(code.split("_")[1]);
           if (System.currentTimeMillis()-OldTime > 60000) {
               return JsonData.buildResult(BizCodeEnum.CODE_LIMITED);
           }
        }
        //利用正则表达式判断邮箱是否合规，合规则发送验证码
        if (CheckUtil.isEmail(to)) {
            String randomCode = CommonUtil.getRandomCode(6);
            stringRedisTemplate.opsForValue().set(key,randomCode+"_"+System.currentTimeMillis(),CODE_EXPIRED, TimeUnit.MILLISECONDS);
            String pwd = String.format(CONTENT, randomCode);
            mailService.sendMail(to,SUBJECT,pwd);
            return JsonData.buildSuccess();
        } else {
            return JsonData.buildResult(BizCodeEnum.CODE_TO_ERROR);
        }

    }


    /**
 * @description:判断前端输入的验证码是否与邮箱发送的验证码一致
 * @author: LZX
 * @date: 2025/2/13 15:55
 * @param: [to, CODE]
 * @return: boolean
 **/
@Override
    public boolean checkCode(String to, String CODE) {
        String key = String.format(CacheKey.CHECK_CODE_KEY, to);
        String code = stringRedisTemplate.opsForValue().get(key);
        //获取redis当中储存的验证码
        if (StringUtils.isNotBlank(code)) {
            String string = code.split("_")[0];
            if (string.equals(CODE)) {
                stringRedisTemplate.delete(key);
                return true;
            }
        }

        return false;
    }
}


