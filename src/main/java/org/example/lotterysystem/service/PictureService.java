package org.example.lotterysystem.service;

import org.springframework.web.multipart.MultipartFile;

public interface PictureService {

    /**
     * 保存图片
     * MultipartFile：上传图片的工具类
     * @return 索引： 上传后的文件名（唯一）
     */
    String savePicture(MultipartFile file);
}
