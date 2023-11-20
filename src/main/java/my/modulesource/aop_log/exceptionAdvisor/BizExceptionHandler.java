package my.modulesource.aop_log.exceptionAdvisor;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * Description : 서비스 에서 발생되는 API Exception을 핸들링 한다.
 * Author      : RedJunHee
 * History     : [2023-11-20] - RedJunHee - Create
 */
@Log4j2
@RestControllerAdvice
public class BizExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Description : 서비스 실행 중 치명적인 Runtime 에러 발생 시 핸들링.
     * Author      : RedJunHee
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("RuntimeExceptionHandler : {} \n StackTrace : " , ex.getMessage(), ex.getStackTrace());
        String response = "시스템에러";

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Description : @RequestBody, @RequestHeader의 매핑 실패일경우  ConstraintViolationException 예외 에러가 발생한다.
     * Author      : RedJunHee
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("파라미터 유효성 체크 실패. : {} \n StackTrace : " , ex.getMessage(), ex.getStackTrace());

        String response;

        if(ex.getConstraintViolations().isEmpty() == false)
        {
            String exceptionMsg = ex.getConstraintViolations().stream()
                    .map(v1 -> v1.getMessage())
                    .collect(Collectors.joining(","));
            response ="파라미터 체크 실패 "+ exceptionMsg;
        }
        else
            response =  response ="파라미터 체크 실패";

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    /**
     * Description : @RequestBody의 매핑 실패일경우  MethodArgumentNotValidException 예외 에러가 발생한다.
     * Author      : RedJunHee
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        log.error("파라미터 유효성 체크 실패. : {} \n StackTrace : " , ex.getMessage(), ex.getStackTrace());
        String response;

        if(ex.getBindingResult().hasErrors())
            response = "파라미터 체크 실패 " + ex.getBindingResult().getFieldError().getDefaultMessage();
        else
            response = "파라미터 체크 실패";

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
