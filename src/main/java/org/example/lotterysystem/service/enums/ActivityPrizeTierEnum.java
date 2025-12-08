package org.example.lotterysystem.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ActivityPrizeTierEnum {

    FIRST_PRIZE(1,"一等奖"),
    SECOND_PRIZE(2,"二等奖"),
    THIRD_PRIZE(3,"三等奖");

    private final Integer code;

    private final String message;

    public static ActivityPrizeTierEnum forName(String name){
        for(ActivityPrizeTierEnum activityPrizeTierEnum : ActivityPrizeTierEnum.values()){
            if(activityPrizeTierEnum.name().equalsIgnoreCase(name)){
                return activityPrizeTierEnum;
            }
        }
        return null;
    }
}
