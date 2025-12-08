package org.example.lotterysystem.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.lotterysystem.common.errorcode.ErrorCode;

//@Data 注解会生成自己的 equals & hashcode 方法， 但这里需要父类的方法。需要使用 @EqualsAndHashCode(callSuper = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends RuntimeException {
    /**异常码
     * @see org.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants
     */
    private Integer code;
    //异常消息
    private String message;

    //为了序列化
    public ServiceException(){

    }

    public ServiceException(Integer code, String message){
        this.code = code;
        this.message = message;
    }

    public ServiceException(ErrorCode errorCode){
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }
}
