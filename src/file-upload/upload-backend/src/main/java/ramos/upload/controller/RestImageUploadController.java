package ramos.upload.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/rest")
@Slf4j
public class RestImageUploadController {

    @Value("${file.dir}")
    private String fileDir;

    @PostMapping("/upload")
    public ResponseEntity<Object> upload(@RequestParam MultipartFile uploadFile) throws IOException {
        log.info("multipartFile={}", uploadFile);

        if (!uploadFile.isEmpty()) {
            String fullPath = fileDir + uploadFile.getOriginalFilename();
            log.info("파일 저장 fullPath={}", fullPath);
            uploadFile.transferTo(new File(fullPath));
        }

        return new ResponseEntity<>(uploadFile.getOriginalFilename(), HttpStatus.OK);
    }
}
