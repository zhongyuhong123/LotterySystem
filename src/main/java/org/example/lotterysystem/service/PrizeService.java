package org.example.lotterysystem.service;

import org.example.lotterysystem.controller.param.CreatePrizeParam;
import org.example.lotterysystem.controller.param.PageParam;
import org.example.lotterysystem.service.dto.PageListDTO;
import org.example.lotterysystem.service.dto.PrizeDTO;
import org.springframework.web.multipart.MultipartFile;

public interface PrizeService {
    /**
     *  创建单个奖品
     * @param param 奖品属性
     * @param picFile 上传的奖品图
     * @return 奖品id
     */
    Long creatPrize(CreatePrizeParam param, MultipartFile picFile);

    /**
     * 翻页查询列表
     * @param param
     * @return
     */
    PageListDTO<PrizeDTO> findPrizeList(PageParam param);
}

