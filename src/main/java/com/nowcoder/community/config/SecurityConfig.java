package com.nowcoder.community.config;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        //忽略对静态资源的访问
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AUTHENTICATION_ADMIN,
                        AUTHENTICATION_MODERATOR,
                        AUTHENTICATION_USER
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        AUTHENTICATION_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete"
                )
                .hasAnyAuthority(
                        AUTHENTICATION_ADMIN
                )
                .anyRequest().permitAll().and().csrf().disable();
        //权限不够的处理
        http.exceptionHandling()
                //没有登录权限不够处理
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                        //判断是否是异步请求
                        String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)){
                            httpServletResponse.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你还没有登录哦!"));
                        }else {
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/login");
                        }
                    }
                })
                //登录后权限不够
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                        //判断是否是异步请求
                        String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)){
                            httpServletResponse.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你还没有访问此功能的权限哦!"));
                        }else {
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/denied");
                        }
                    }
                });
        /*
        Spring Security底层默认会拦截/logout请求，进行退出处理
        只有覆盖它默认的逻辑，才能执行自己写的退出代码
         */
        http.logout().logoutUrl("/securityLogout");
    }
}
