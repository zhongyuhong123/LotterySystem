package org.example.lotterysystem.controller;


import jakarta.validation.Valid;
import org.example.lotterysystem.common.Util.JacksonUtil;
import org.example.lotterysystem.common.errorcode.ControllerErrorCodeConstants;
import org.example.lotterysystem.common.exception.ControllerException;
import org.example.lotterysystem.common.pojo.CommonResult;
import org.example.lotterysystem.controller.param.CreatePrizeParam;
import org.example.lotterysystem.controller.param.PageParam;
import org.example.lotterysystem.controller.result.FindPrizeListResult;
import org.example.lotterysystem.service.PictureService;
import org.example.lotterysystem.service.PrizeService;
import org.example.lotterysystem.service.dto.PageListDTO;
import org.example.lotterysystem.service.dto.PrizeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@RestController
public class PrizeController {

    private static final Logger logger = LoggerFactory.getLogger(PrizeController.class);

    @Autowired
    private PictureService pictureService;
    @Autowired
    private PrizeService prizeService;

    @RequestMapping("/pic/upload")
    public String uploadPic(MultipartFile file) {
        return pictureService.savePicture(file);
    }

    /**
     * 创建奖品
     * RequestPart: 用于接收表单数据的 multipart/form-data
     *
     * @param param
     * @param picFile
     * @return
     */
    @RequestMapping("/prize/create")
    public CommonResult<Long> createPrize(@Valid @RequestPart("param") CreatePrizeParam param,
                                          @RequestPart("prizePic") MultipartFile picFile){
        logger.info("createPrize CreatPrizeParam: {}", JacksonUtil.writeValueAsString(param));
        return CommonResult.success(prizeService.creatPrize(param, picFile));
    }

    @RequestMapping("/prize/find-list")
    public CommonResult<FindPrizeListResult> findPrizeList(PageParam param) {
        logger.info("findPrizeList PageParam: {}", JacksonUtil.writeValueAsString(param));

        PageListDTO<PrizeDTO> pageListDTO = prizeService.findPrizeList(param);
        return CommonResult.success(convertToFindPrizeListResult(pageListDTO));
    }

    private FindPrizeListResult convertToFindPrizeListResult(PageListDTO<PrizeDTO> pageListDTO) {
        if(pageListDTO == null) {
            throw new ControllerException(ControllerErrorCodeConstants.FIND_PRIZE_LIST_ERROR);
        }

        FindPrizeListResult result = new FindPrizeListResult();
        result.setTotal(pageListDTO.getTotal());
        result.setRecords(
                pageListDTO.getRecords().stream()
                        .map(prizeDTO -> {
                            FindPrizeListResult.PrizeInfo prizeInfo = new FindPrizeListResult.PrizeInfo();
                            prizeInfo.setPrizeId(prizeDTO.getPrizeId());
                            prizeInfo.setPrizeName(prizeDTO.getName());
                            prizeInfo.setDescription(prizeDTO.getDescription());
                            prizeInfo.setImageUrl(prizeDTO.getImageUrl());
                            prizeInfo.setPrice(prizeDTO.getPrice());
                            return prizeInfo;
                        }).collect(Collectors.toList())
        );
        return result;
    }
}
