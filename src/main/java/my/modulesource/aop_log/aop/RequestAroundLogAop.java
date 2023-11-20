package my.modulesource.aop_log.aop;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Class       : RqeustAroundLogAop (AOP)
 *  Author      : RedJunHee
 *  Description : com.dike.modules.controller.* 패키지 내 *Contorller.class 모든 클래스의
 *  메소드 모두 가로챔 => Request 정보 및 처리 결과를 LogService를 통해 Database에 저장
 *  History     : [2023-11-20] - RedJunHee - create class
 */

@Aspect
@Component
@Log4j2
public class RequestAroundLogAop {

    private final ObjectMapper om ;
    //private final LogService logService;

    @Autowired
    public RequestAroundLogAop(ObjectMapper om) {
        this.om = om;
    }

    //  execution(* com.map.mutual.side.*.controller 하위 패키지 내에
    //   *Controller 클래스의 모든 메서드 Around => Pointcut 설정
    @Around(value = "execution(* my.modulesource.aop_log.controller..*Controller.*(..))" )
    public Object ApiLog(ProceedingJoinPoint joinPoint) throws Throwable { // 파라미터 : 프록시 대상 객체의 메서드를 호출할 때 사용

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String inputParam = getWrapperParamJson(request.getParameterMap());
        String  outputMessage = "" ;
        char apiStatus = 'Y';
        String methodName = "";
        LocalDateTime date = LocalDateTime.now();
        String loginId = "";
        long elapsedTime = 0L;
        StringBuilder apiResultDescription = new StringBuilder();
        // joinPoint 리턴 객체 담을 변수
        Object retValue = null;

        StopWatch stopWatch = new StopWatch();

        try {

            methodName  = joinPoint.getSignature().getName();   // 메소드 이름 => Api명

            // 서비스 처리 시간 기록 시작
            stopWatch.start();
            retValue = joinPoint.proceed();   // 실제 대상 객체의 메서드 호출

            outputMessage = om.writeValueAsString( ((ResponseEntity)retValue).getBody());

        }
        catch(ConstraintViolationException ex){
            // 에러가 아닌 경우
            apiStatus = 'N';

            String response;

            if(ex.getConstraintViolations().isEmpty() == false)
            {
                String exceptionMsg = ex.getConstraintViolations().stream()
                        .map(v1 -> v1.getMessage())
                        .collect(Collectors.joining(","));
                response = "파라미터 체크 실패 " + exceptionMsg;
            }
            else
                response = "파라미터 체크 실패";

            log.info("[{}]log desc : {}", ThreadContext.get("logId").toString(),response);
            throw ex;
        }
        catch(MethodArgumentNotValidException ex){
            apiStatus = 'N';

            String response;

            if(ex.getBindingResult().hasErrors())
                response = "파라미터 체크 실패 " + ex.getBindingResult().getFieldError().getDefaultMessage();
            else
                response = "파라미터 체크 실패";

            log.info("[{}]log desc : {}", ThreadContext.get("logId").toString(),response);
            throw ex;
        }
        catch(Exception ex) {
            apiStatus='N';
            String response = "시스템 에러 " + ex.getMessage();
            //Exception
            log.info("[{}]log desc : {}", ThreadContext.get("logId").toString(), response);
            throw ex;
        }
        finally {
            // 서비스 처리 시간 기록 종료
            stopWatch.stop();

            //api 처리 정보 => INPUT + OUTPUT   ** Exception이 떨어졌을때 Exception정보도 담는지 확인 필요 함.
            apiResultDescription.append("[Request URI] - ").append(request.getRequestURI())
                    .append("\n[INPUT]").append(System.lineSeparator())
                    .append(inputParam).append(System.lineSeparator())
                    .append("[OUTPUT]").append(System.lineSeparator())
                    .append(outputMessage).append(System.lineSeparator());

            elapsedTime = stopWatch.getTotalTimeMillis();
            String apiDesc = "";
            if(apiResultDescription.length() > 4000)
                apiDesc = apiResultDescription.toString().substring(0,4000);
            else
                apiDesc = apiResultDescription.toString();

            log.info("[{}]log desc : {}", ThreadContext.get("logId").toString(), apiDesc);
            //API_LOG담을 객체 생성 ( "SUID",  2022-01-14T12:55:22, "save", 'Y', [INPUT] [메서드 input] [OUTPUT] [메서드 output] , 3.0
//            ApiLog data = ApiLog.builder()
//                    .suid(suid)
//                    .apiName(methodName)
//                    .apiDesc(apiDesc)
//                    .apiStatus(apiStatus)
//                    .processTime((float) (elapsedTime*0.001))
//                    .build();
//            // INSERT
//            logService.InsertApiLog(data);
        }

        return retValue;
    }
    private String getWrapperParamJson(Map<String,String[]> wrapperParams) throws JsonProcessingException {
        Map<String,String> params = new HashMap<>();

        for(String key : wrapperParams.keySet()){
            String[] values = wrapperParams.get(key);
            String sumValue = "";
            for(String value : values)
                if(sumValue.equals("") == false)
                    sumValue+=", "+value;
                else
                    sumValue += value;
                params.put(key,sumValue);
        }
        return om.writeValueAsString(params);
    }

}
