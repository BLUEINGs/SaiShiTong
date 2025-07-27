package com.blueing.sports_meet_system.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;

public class JwtUtil {

    private static final String secret="769BA027-5DDB-0BF9-C2C1-2B345AE98857";

    public static String generateJwt(String userName,String password,Integer uid,Integer smId,Date date){
        HashMap<String, Object> claims=new HashMap<>();
        claims.put("userName",userName);
        claims.put("password",password);
        claims.put("uid",uid);
        claims.put("smId",smId);
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256,secret)
                .setClaims(claims)
                .setExpiration(date)
                .compact();
    }

    public static Claims parseJwt(String token){
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

}
