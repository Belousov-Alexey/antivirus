package ru.mtuci.bvt_demo;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileScannerService {
    private final SignatureRepository signatureRepository;
    private static final int WINDOW_SIZE = 8; // Размер окна для first_bytes
    private static final int BASE = 256; // Основание для хэша Рабина-Карпа
    private static final long MOD = 1000000007L; // Модуль для хэша

    public FileScannerService(SignatureRepository signatureRepository) {
        this.signatureRepository = signatureRepository;
    }

    // Добавление сигнатуры
    public SignatureEntity addSignature(SignatureEntity signature) {
        if (signature.getUpdatedAt() == null) {
            signature.setUpdatedAt(LocalDateTime.now());
        }
        if (signature.getStatus() == null) {
            signature.setStatus("ACTUAL");
        }
        return signatureRepository.save(signature);
    }

    // Получение всех сигнатур
    public List<SignatureEntity> getAllSignatures() {
        return signatureRepository.findByStatus("ACTUAL");
    }

    // Сканирование файла
    public List<SignatureScanResult> scanFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("scan_", ".tmp");
        file.transferTo(tempFile);

        List<SignatureScanResult> results = new ArrayList<>();
        List<SignatureEntity> signatures = signatureRepository.findByStatus("ACTUAL");

        try (RandomAccessFile raf = new RandomAccessFile(tempFile, "r")) {
            long fileLength = raf.length();
            byte[] buffer = new byte[8192];
            long rollingHash = 0;
            byte[] window = new byte[WINDOW_SIZE];
            int bytesRead;
            long offset = 0;

            while ((bytesRead = raf.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    if (offset >= WINDOW_SIZE) {
                        rollingHash = updateRollingHash(rollingHash, window[0], buffer[i]);
                        System.arraycopy(window, 1, window, 0, WINDOW_SIZE - 1);
                        window[WINDOW_SIZE - 1] = buffer[i];
                    } else {
                        window[(int) offset] = buffer[i];
                        rollingHash = (rollingHash * BASE + (buffer[i] & 0xFF)) % MOD;
                    }

                    if (offset >= WINDOW_SIZE - 1) {
                        for (SignatureEntity signature : signatures) {
                            if (checkSignatureMatch(rollingHash, window, signature, raf, offset - WINDOW_SIZE + 1, fileLength)) {
                                results.add(new SignatureScanResult(
                                        signature.getId(),
                                        signature.getThreatName(),
                                        offset - WINDOW_SIZE + 1,
                                        offset - WINDOW_SIZE + 1 + WINDOW_SIZE + signature.getRemainderLength(),
                                        true
                                ));
                            }
                        }
                    }
                    offset++;
                }
            }
        } finally {
            tempFile.delete();
        }
        return results;
    }

    private long updateRollingHash(long oldHash, byte oldByte, byte newByte) {
        long highestPower = (long) Math.pow(BASE, WINDOW_SIZE - 1) % MOD;
        long newHash = (oldHash - (oldByte & 0xFF) * highestPower) * BASE + (newByte & 0xFF);
        return (newHash % MOD + MOD) % MOD;
    }

    private boolean checkSignatureMatch(long rollingHash, byte[] window, SignatureEntity signature, RandomAccessFile raf, long offset, long fileLength) throws IOException {
        byte[] signatureFirstBytes = signature.getFirstBytes();
        if (signatureFirstBytes.length != WINDOW_SIZE) {
            return false; // Убедимся, что длина совпадает
        }

        // Сравнение первых 8 байт
        for (int i = 0; i < WINDOW_SIZE; i++) {
            if (window[i] != signatureFirstBytes[i]) {
                return false;
            }
        }

        // Проверка смещений
        long startOffset = offset;
        long endOffset = offset + WINDOW_SIZE + signature.getRemainderLength();
        if (startOffset < signature.getOffsetStart() || endOffset > signature.getOffsetEnd()) {
            return false;
        }

        // Проверка "хвоста"
        int remainderLength = signature.getRemainderLength();
        if (remainderLength > 0 && offset + WINDOW_SIZE + remainderLength <= fileLength) {
            byte[] remainder = new byte[remainderLength];
            raf.seek(offset + WINDOW_SIZE);
            int bytesRead = raf.read(remainder);
            if (bytesRead != remainderLength) {
                return false;
            }
            String computedHash = Utils.sha256Hex(remainder);
            return computedHash.equalsIgnoreCase(signature.getRemainderHash());
        }
        return remainderLength == 0;
    }
}