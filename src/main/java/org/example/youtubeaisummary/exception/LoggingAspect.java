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
        long startTime = System.currentTimeMillis();
        logger.info("Entering {}", methodName);
        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - startTime;
            logger.info("Exiting {} - executed in {} ms", methodName, elapsedTime);
            return result;
        } catch (Throwable t) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            logger.error("Exception in {} - executed in {} ms", methodName, elapsedTime, t);
            throw t;
        }
    }
}
