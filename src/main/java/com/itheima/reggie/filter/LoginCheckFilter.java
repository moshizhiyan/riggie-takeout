package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 校验是否登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    // 路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //获取本次请求URI
        String uri = request.getRequestURI();
        // 无需拦截的请求
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/login",
                "/user/sendMsg",
        };

        // 判断是否需要处理
        boolean check = checkPath(urls, uri);

        // 如果不需要则放行
        if(check){
            filterChain.doFilter(request, response);
            return;
        }

        // 判断后台用户登录状态，如果已登录则放行
        if (request.getSession().getAttribute("employee") != null){
            //存储用户id到ThreadLocal中
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
//            log.info("userId is: "+empId);

            filterChain.doFilter(request, response);
            return;
        }

        // 判断手机端用户登录状态，如果已登录则放行
        if (request.getSession().getAttribute("user") != null){
            //存储用户id到ThreadLocal中
            Long usrId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(usrId);
//            log.info("userId is: "+empId);

            filterChain.doFilter(request, response);
            return;
        }

        // 如果未登录则返回数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    /**
     * 检查路径是否匹配
     * @param urls
     * @param uri
     * @return
     */
    public boolean checkPath(String[] urls, String uri){
        for (String url : urls) {
            if(PATH_MATCHER.match(url, uri)){
                return true;
            }
        }
        return false;
    }
}
