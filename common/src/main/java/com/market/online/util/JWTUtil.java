package com.market.online.util;


import com.market.online.vo.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;


import java.util.Date;

/**

 **/

@Slf4j
public class JWTUtil {


    /**
     * token 过期时间 5分钟
     */
    private static final long EXPIRE = 1000 * 60 * 5;

    private static final String JWT_SECRET_ENV = "JWT_SECRET";

    /**
     * 令牌前缀
     */
    private static final String TOKEN_PREFIX = "buka";

    /**
     * subject
     */
    private static final String SUBJECT = "buka";


    /**
     * 根据用户信息，生成令牌
     *
     * @param loginUser
     * @return
     */
    public static String geneJsonWebToken(LoginUser loginUser) {

        if (loginUser == null) {
            throw new NullPointerException("loginUser对象为空");
        }

        String token = Jwts.builder().setSubject(SUBJECT)
                .claim("head_img", loginUser.getHeadImg())
                .claim("id", loginUser.getId())
                .claim("name", loginUser.getName())
                .claim("mail", loginUser.getMail())
                .claim("admin", loginUser.getAdmin())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(SignatureAlgorithm.HS256, getSecret()).compact();

        token = TOKEN_PREFIX + token;
        return token;
    }


    /**
     * 校验token的方法
     *
     * @param token
     * @return
     */
    public static Claims checkJWT(String token) {

        try {

            final Claims claims = Jwts.parser()
                    .setSigningKey(getSecret())
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody();

            return claims;

        } catch (Exception e) {
            log.info("jwt token解密失败");
            return null;
        }
    }

    private static String getSecret() {
        String secret = System.getenv(JWT_SECRET_ENV);
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT_SECRET is required. Configure it in .env or service environment.");
        }
        return secret;
    }
}
