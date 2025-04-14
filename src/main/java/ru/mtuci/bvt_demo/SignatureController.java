package ru.mtuci.bvt_demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/signatures")
public class SignatureController {
    private final FileScannerService fileScannerService;

    public SignatureController(FileScannerService fileScannerService) {
        this.fileScannerService = fileScannerService;
    }

    @PostMapping("/add")
    public ResponseEntity<SignatureEntity> addSignature(@RequestBody SignatureDTO signatureDTO) {
        try {
            SignatureEntity signature = signatureDTO.toEntity();
            SignatureEntity savedSignature = fileScannerService.addSignature(signature);
            return ResponseEntity.ok(savedSignature);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SignatureEntity> updateSignature(@PathVariable Long id, @RequestBody SignatureDTO signatureDTO) {
        try {
            SignatureEntity signature = signatureDTO.toEntity();
            SignatureEntity updatedSignature = fileScannerService.updateSignature(id, signature);
            return ResponseEntity.ok(updatedSignature);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSignature(@PathVariable Long id) {
        try {
            fileScannerService.deleteSignature(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<SignatureEntity>> getAllSignatures() {
        List<SignatureEntity> signatures = fileScannerService.getAllSignatures();
        return ResponseEntity.ok(signatures);
    }
}