package org.example.lotterysystem;


import org.example.lotterysystem.controller.param.DrawPrizeParam;
import org.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import org.example.lotterysystem.service.DrawPrizeService;
import org.example.lotterysystem.service.activitystatus.ActivityStatusManager;
import org.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import org.example.lotterysystem.service.dto.WinningRecordDTO;
import org.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import org.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class DrawPrizeTest {

    @Autowired
    private DrawPrizeService drawPrizeService;
    @Autowired
    private ActivityStatusManager activityStatusManager;

    @Test
    void drawPrize() {

        DrawPrizeParam param = new DrawPrizeParam();
        param.setActivityId(15L);
        param.setPrizeId(7L);
        param.setWinningTime(new Date());

        List<DrawPrizeParam.Winner> winnerList = new ArrayList<>();
        DrawPrizeParam.Winner winner = new DrawPrizeParam.Winner();
        winner.setUserId(43L);
        winner.setUserName("杨过");
        winnerList.add(winner);
        param.setWinnerList(winnerList);

        drawPrizeService.drawPrize(param);

        // 1、正向流程
        // 2、处理过程中发生的异常：回滚
        // 3、处理过程中发生异常：消息堆积 -》处理异常 -》消息重发

    }

    @Test
    void statusConvert(){

        ConvertActivityStatusDTO convertActivityStatusDTO = new ConvertActivityStatusDTO();
        convertActivityStatusDTO.setActivityId(15L);
        convertActivityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.COMPLETED);
        convertActivityStatusDTO.setPrizeId(8L);
        convertActivityStatusDTO.setTargetPrizeStatus(ActivityPrizeStatusEnum.COMPLETED);
        List<Long> userIds = Arrays.asList(42L);
        convertActivityStatusDTO.setUserIds(userIds);
        convertActivityStatusDTO.setTargetUserStatus(ActivityUserStatusEnum.COMPLETED);
        activityStatusManager.handlerEvent(convertActivityStatusDTO);
    }

    @Test
    void saveWinningRecords(){
        DrawPrizeParam param = new DrawPrizeParam();
        param.setActivityId(15L);
        param.setPrizeId(7L);
//        param.setPrizeTiers("FIRST_PRIZE");
//        param.setPrizeTiers("SECOND_PRIZE");
        param.setWinningTime(new Date());

        List<DrawPrizeParam.Winner> winnerList = new ArrayList<>();
        DrawPrizeParam.Winner winner = new DrawPrizeParam.Winner();
        winner.setUserId(43L);
        winner.setUserName("杨过");
        winnerList.add(winner);
        param.setWinnerList(winnerList);

        drawPrizeService.saveWinnerRecords(param);
    }

    @Test
    void showWinningRecords(){
        ShowWinningRecordsParam param = new ShowWinningRecordsParam();
        param.setActivityId(15L);
        List<WinningRecordDTO> winningRecordDTOList = drawPrizeService.getRecords(param);
        for(WinningRecordDTO dto : winningRecordDTOList){
            System.out.println(dto.getWinnerName()+"_"
                    +dto.getPrizeName()+"_"+dto.getPrizeTier());
        }
    }
}
