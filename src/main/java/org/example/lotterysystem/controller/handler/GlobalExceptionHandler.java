package org.example.lotterysystem.controller.handler;

import org.example.lotterysystem.common.errorcode.GlobalErrorCodeConstants;
import org.example.lotterysystem.common.exception.ControllerException;
import org.example.lotterysystem.common.exception.ServiceException;
import org.example.lotterysystem.common.pojo.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;



@RestControllerAdvice //可以捕获全局异常
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = ServiceException.class)//指定可以捕获异常的类
    public CommonResult<?> serviceException(ServiceException e) {
        //打印错误日志
        logger.error("ServiceException", e);
        //构造错误结果
        return CommonResult.error(
                GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                e.getMessage());
    }

    @ExceptionHandler(value = ControllerException.class)
    public CommonResult<?> controllerException(ControllerException e) {
        //打印错误日志
        logger.error("ControllerException", e);
        //构造错误结果
        return CommonResult.error(
                GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public CommonResult<?> Exception(Exception e) {
        //打印错误日志
        logger.error("服务异常", e);
        //构造错误结果
        return CommonResult.error(
                GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                e.getMessage());
    }

}
