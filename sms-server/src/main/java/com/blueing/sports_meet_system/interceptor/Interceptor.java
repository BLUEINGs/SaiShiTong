package com.blueing.sports_meet_system.interceptor;

import com.blueing.sports_meet_system.exception.businessEception.ExpiredLoginException;
import com.blueing.sports_meet_system.exception.businessEception.NoSmIdException;
import com.blueing.sports_meet_system.pojo.Result;
import com.blueing.sports_meet_system.pojo.User;
import com.blueing.sports_meet_system.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class Interceptor extends InterceptorRegistry implements HandlerInterceptor {

    @Autowired
    private ObjectMapper objectMapper;

    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(204);
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS");
            response.setHeader("Access-Control-Max-Age", "86400");
            response.setHeader("Access-Control-Allow-Headers", "*");
            log.info("接收到了OPTIONS请求，响应完毕");
            return false;
        }

        //包装缓存下响应体，并将这个缓存包装记录进请求中
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        request.setAttribute("cachedResponse", responseWrapper);

        String token = request.getHeader("Token");
        try {
            Claims claims = JwtUtil.parseJwt(token);
            User user = new User((Integer) claims.get("uid"), (String) claims.get("userName"), (String) claims.get("password"), null, null, null, null, null, (Integer) claims.get("smId"), null);
            currentUser.set(user);
            log.info("当前用户为{}", currentUser.get());
            /**
             * 这里会把token解析成User对象，该对象即前端操作的那个用户
             * 获取该对象可以调用Interceptor类提供的静态方法getCurrentUser()，在任意类中都可以获取
             * */
            request.setAttribute("currentUser", user);
        }catch(ExpiredJwtException e){
            log.info("登录信息已过期，请重新登录");
            throw new ExpiredLoginException("登录已失效，请重新登录");
        }
        catch (Exception e) {
            log.info("当前用户没有登录");
            User user = new User();
            user.setUid(0);
            String smId = request.getHeader("SmId");
            if (smId != null) {
                user.setSmId(Integer.parseInt(smId));
                currentUser.set(user);
                log.info("当前构建的游客对象{}", user);
            } else {
                log.info("当前用户也没有传递smId值");
                throw new NoSmIdException("未登录用户请先切换到某场运动会的视图");
            }
//            throw new JwtException();
        }

        return true;
    }

    public static User getCurrentUser() {
        return currentUser.get();
    }

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    static void returnLowPower(HttpServletResponse response, User currentUser, Logger log, ObjectMapper objectMapper) throws IOException {
        log.info("uid：{}权限不足", currentUser.getUid());
        Result<String> result = new Result<>(0, "权限不足，请联系您的上属或本项目开发者", "POWER_LOW");
        response.setStatus(400);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //从请求中获取记录的响应缓存包装
        ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) request.getAttribute("cachedResponse");

        // 读取响应内容
        byte[] responseBody = responseWrapper.getContentAsByteArray();
        String body = new String(responseBody, StandardCharsets.UTF_8);
        System.out.println(body);
        //不知道如何解决，先不管了
        try {
            Result<Object> result = objectMapper.readValue(body, Result.class);
            if (result.getMessage().equals("POWER_LOW")) {
                returnLowPower(response, currentUser.get(), log, objectMapper);
            }
        } catch (Exception e) {
            log.info("正确返回");
        }
        currentUser.set(new User());

        /*try {
            responseWrapper.copyBodyToResponse();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

//        System.out.println("Response Body: " + body);

        // 重要：将数据写回原始响应流（否则客户端收不到响应）

    }
}