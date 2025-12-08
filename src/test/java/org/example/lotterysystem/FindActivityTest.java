package org.example.lotterysystem;

import org.example.lotterysystem.service.ActivityService;
import org.example.lotterysystem.service.dto.ActivityDTO;
import org.example.lotterysystem.service.dto.ActivityDetailDTO;
import org.example.lotterysystem.service.enums.ActivityPrizeTierEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;

@SpringBootTest
public class FindActivityTest {

    @Autowired
    private ActivityService activityService;

    @Test
    void test(){

//        ActivityDetailDTO detailDTO = activityService.getActivityDetail(7L);
//
//        detailDTO.getPrizeDTOList().stream().map(prizeDTO -> {
//            System.out.println("法一 ：activitId = 7的等级：" + prizeDTO.getTier());
//            System.out.println("法二 ： activitId = 7的等级：" + ActivityPrizeTierEnum.forName(prizeDTO.getTiers()).getCode());
//            return new ActivityDTO();
//        }).collect(Collectors.toList());

    }
}