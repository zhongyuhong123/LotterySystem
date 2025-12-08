package org.example.lotterysystem.dao.dataobject;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BaseDO implements Serializable {

    private Long id;//主键
    private Date gmtCre;//创建时间
    private Date gmtM;//修改时间

}
