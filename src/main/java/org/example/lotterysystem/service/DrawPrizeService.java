package org.example.lotterysystem.service;

import org.example.lotterysystem.controller.param.DrawPrizeParam;
import org.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import org.example.lotterysystem.dao.dataobject.WinningRecordDO;
import org.example.lotterysystem.service.dto.WinningRecordDTO;

import java.util.List;

public interface DrawPrizeService {

    void drawPrize(DrawPrizeParam param);

    Boolean checkPrizeParam(DrawPrizeParam param);

    List<WinningRecordDO> saveWinnerRecords(DrawPrizeParam param);

    void deleteWinnerRecords(Long activityId, Long prizeId);

    List<WinningRecordDTO> getRecords(ShowWinningRecordsParam param);
}
