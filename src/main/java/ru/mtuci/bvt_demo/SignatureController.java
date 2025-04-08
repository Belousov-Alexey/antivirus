package ru.mtuci.bvt_demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/signatures")
public class SignatureController {
    private final FileScannerService fileScannerService;

    public SignatureController(FileScannerService fileScannerService) {
        this.fileScannerService = fileScannerService;
    }

    @PostMapping("/add")
    public ResponseEntity<SignatureEntity> addSignature(@RequestBody SignatureEntity signature) {
        // Устанавливаем значения по умолчанию
        signature.setUpdatedAt(LocalDateTime.now());
        signature.setStatus("ACTUAL");
        SignatureEntity savedSignature = fileScannerService.addSignature(signature);
        return ResponseEntity.ok(savedSignature);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SignatureEntity>> getAllSignatures() {
        List<SignatureEntity> signatures = fileScannerService.getAllSignatures();
        return ResponseEntity.ok(signatures);
    }
}