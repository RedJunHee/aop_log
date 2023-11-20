package my.modulesource.aop_log.filter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Class       : HttpServletFilter
 * Author      : RedJunHee
 * Description : HttpServletWrapper로 요청 정보를 변경 + 필터로 래퍼클래스를 프로세스하게 만듬.
 * History     : [2023-11-20] - RedJunHee - Class Create

 */
@Component
@Order(value = 101)
public class HttpServletFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
        // Do nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpRequestReadableParamWrapper wrapper = new HttpRequestReadableParamWrapper((HttpServletRequest) servletRequest);

        filterChain.doFilter(wrapper, servletResponse);

    }

    @Override
    public void destroy() {
        // Do nothing
    }

}
