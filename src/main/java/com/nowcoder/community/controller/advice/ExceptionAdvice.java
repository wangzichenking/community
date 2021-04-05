package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 统一异常处理
 */
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionAdvice.class);

    /**
     * 统一异常处理
     * @param e
     * @param request
     * @param response
     */
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request,HttpServletResponse response) throws IOException {
        LOGGER.error("服务器发生异常："+e.getMessage());
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            LOGGER.error(stackTraceElement.toString());
        }
        /*
        判断请求是否是异步请求
         */
        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)){
            //这是一个异步请求
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常"));
        }else {
            response.sendRedirect(request.getContextPath()+"/error");
        }
    }
}
