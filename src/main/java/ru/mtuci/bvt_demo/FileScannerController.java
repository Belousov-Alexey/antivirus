package ru.mtuci.bvt_demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileScannerController {
    private final FileScannerService fileScannerService;

    public FileScannerController(FileScannerService fileScannerService) {
        this.fileScannerService = fileScannerService;
    }

    @PostMapping("/scan")
    public ResponseEntity<List<SignatureScanResult>> scanFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        try {
            List<SignatureScanResult> results = fileScannerService.scanFile(file);
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(new SignatureScanResult(null, "Error: " + e.getMessage(), 0, 0, false)));
        }
    }
}