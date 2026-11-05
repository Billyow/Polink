package com.polink.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String storeImage(MultipartFile file, String subFolder);
}
