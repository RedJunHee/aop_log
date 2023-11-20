package my.modulesource.aop_log.filter;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;
import java.util.UUID;

/**
 * Class       : Log4j2MDCFilter
 * Author      : RedJunHee
 * Description : Filter 앞부분에서 Log4j2의 일관성 있는 로그 분석을 위해
 * org.apache.logging.log4j.ThreadContext에 "logId"를 부여함.
 *
 * History     : [2023-11-20] - RedJunHee - Class Create
 */

@Order(value = 100)
@Component
public class Log4j2MDCFilter implements Filter {
    private final static Logger logger = LogManager.getLogger(Log4j2MDCFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        logger.info("Filter - Log4j2MDCFilter Init");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        ThreadContext.put("logId", UUID.randomUUID().toString());
        filterChain.doFilter(servletRequest,servletResponse);
        ThreadContext.clearAll();
    }

    @Override
    public void destroy() {

    }

}
