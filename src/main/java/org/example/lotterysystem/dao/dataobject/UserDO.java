package org.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDO extends BaseDO{
    private String userName;
    private String email;
    private Encrypt phoneNumber;
    private String password;
    private String identity;
}
