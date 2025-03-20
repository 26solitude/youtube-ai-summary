package org.example.youtubeaisummary.exception;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // service 패키지 내 모든 메서드 호출에 적용
    @Around("execution(* org.example.youtubeaisummary.service..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        logger.info("Entering " + methodName);
        try {
            Object result = joinPoint.proceed();
            logger.info("Exiting " + methodName);
            return result;
        } catch (Throwable t) {
            logger.error("Exception in " + methodName, t);
            throw t;
        }
    }
}
