package com.nowcoder.community.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AlphaInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaInterceptor.class);

    /*
    在Controller之前执行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOGGER.debug("preHandler："+handler.toString());
        return true;
    }

    /*
    调用完Controller之后执行
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        LOGGER.debug("postHandler："+handler.toString());
    }

    /*
    在TemplateEngine之后执行
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LOGGER.debug("afterCompletion："+handler.toString());
    }
}
