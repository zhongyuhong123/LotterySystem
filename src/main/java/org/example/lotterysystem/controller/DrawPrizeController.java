package org.example.lotterysystem.controller;


import org.example.lotterysystem.common.Util.JacksonUtil;
import org.example.lotterysystem.common.pojo.CommonResult;
import org.example.lotterysystem.controller.param.DrawPrizeParam;
import org.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import org.example.lotterysystem.controller.result.WinningRecordResult;
import org.example.lotterysystem.service.DrawPrizeService;
import org.example.lotterysystem.service.dto.WinningRecordDTO;
import org.example.lotterysystem.service.enums.ActivityPrizeTierEnum;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DrawPrizeController {

    private static final Logger logger = LoggerFactory.getLogger(DrawPrizeController.class);

    @Autowired
    private DrawPrizeService drawPrizeService;

    @RequestMapping("/draw-prize")
    public CommonResult<Boolean> drawPrize(
            @Validated @RequestBody DrawPrizeParam param) {
        logger.info("drawPrize DrawPrizeParam: {}", param);
        drawPrizeService.drawPrize(param);

        return CommonResult.success(true);
    }

    @RequestMapping("/winning-records/show")
    public CommonResult<List<WinningRecordResult>> showWinningRecords(
            @Validated @RequestBody ShowWinningRecordsParam param){
        logger.info("showWinningRecords ShowWinningRecordsParam: {}", JacksonUtil.writeValueAsString(param));
        List<WinningRecordDTO> winningRecordDTOList = drawPrizeService.getRecords(param);
        return CommonResult.success(convertToWinningRecordResultList(winningRecordDTOList));
    }

    private List<WinningRecordResult> convertToWinningRecordResultList(List<WinningRecordDTO> winningRecordDTOList) {
        if(CollectionUtils.isEmpty(winningRecordDTOList)){
            return Arrays.asList();
        }
        return winningRecordDTOList.stream()
                .map(winningRecordDTO -> {
                    WinningRecordResult result = new WinningRecordResult();
                    result.setWinnerId(winningRecordDTO.getWinnerId());
                    result.setWinnerName(winningRecordDTO.getWinnerName());
                    result.setPrizeName(winningRecordDTO.getPrizeName());
                    result.setPrizeTier(winningRecordDTO.getPrizeTier().getMessage());
                    result.setWinningTime(winningRecordDTO.getWinningTime());
                    return result;
                }).collect(Collectors.toList());
    }
}
