package com.buka.controller;

import com.buka.enums.BizCodeEnum;
import com.buka.service.MailService;
import com.buka.service.NotifyService;
import com.buka.util.CommonUtil;
import com.buka.util.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import com.google.code.kaptcha.Producer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

/**
 * @className: NotifyController
 * @author: LZX
 * @date: 2025/2/10 10:19
 * @Version: 1.0
 * @description:生成处理验证码
 */
@RestController
@RequestMapping("api/notify/v1")
@Slf4j
public class NotifyController {
    @Autowired
    @Qualifier("kaptcha")
    private Producer producer;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private NotifyService notifyService;
    /**
     * 使用10分钟
     */
    private static final long CAPTCHA_CODE_EXPIRED = 60 * 1000 * 10;
    /**
     * @description:生成验证码图片
     * @author: LZX
     * @date: 2025/2/10 10:28
     * @param: [response]
     * @return: void
     **/
    @GetMapping("/captcha")
    public void captcha(HttpServletResponse response,HttpServletRequest request){
        //生成验证码答案
        String text = producer.createText();
        log.info("验证码为:{}", text);
        //将验证码储存进redis
        String key = getCaptchaKey(request);
        stringRedisTemplate.opsForValue().set(key,text,CAPTCHA_CODE_EXPIRED, TimeUnit.MILLISECONDS);
        //生成验证码图片
        BufferedImage image = producer.createImage(text);
        ServletOutputStream out = null;
        try {
          out = response.getOutputStream();
            ImageIO.write(image, "jpg", out);
            out.flush();
            out.close();
        }catch (Exception e) {
            log.info("返回图片出错:{}",e);
        }
    }
    /**
     * @description:生成储存进redis的key值
     * @author: LZX
     * @date: 2025/2/10 11:06
     * @param: [request]
     * @return: java.lang.String
     **/
    public String getCaptchaKey(HttpServletRequest request) {
        //1.获取ip地址
        String ip = CommonUtil.getIpAddr(request);
        //获取游览器信息
        String header = request.getHeader("User-Agent");
        return "user-service:captcha:"+CommonUtil.MD5(ip+header);

    }
    /**
     * @description:发送邮箱验证码
     * @author: LZX
     * @date: 2025/2/13 12:39
     * @param: [to, captcha, request]
     * @return: com.buka.util.JsonData
     **/
    @RequestMapping("/send_code")
    public JsonData sendCode(@RequestParam(value = "to", required = true) String to, @RequestParam(value = "captcha", required = true) String captcha, HttpServletRequest request) {
        String key = getCaptchaKey(request);
        String string = stringRedisTemplate.opsForValue().get(key);
        log.info(to+captcha);
        //判断验证码是否一致
        if (to != null && captcha != null && string.equalsIgnoreCase(captcha)) {
            stringRedisTemplate.delete(key);
            JsonData jsonData = notifyService.sendCode(to);
            return jsonData;
        } else {
            return JsonData.buildResult(BizCodeEnum.CODE_CAPTCHA);
        }
    }
}


