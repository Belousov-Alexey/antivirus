package ru.mtuci.bvt_demo;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FileScannerService {
    private final SignatureRepository signatureRepository;
    private final SignatureHistoryRepository historyRepository;
    private final SignatureAuditRepository auditRepository;
    private static final int WINDOW_SIZE = 8;
    private static final int BASE = 256;
    private static final long MOD = 1000000007L;

    public FileScannerService(SignatureRepository signatureRepository,
                              SignatureHistoryRepository historyRepository,
                              SignatureAuditRepository auditRepository) {
        this.signatureRepository = signatureRepository;
        this.historyRepository = historyRepository;
        this.auditRepository = auditRepository;
    }

    // Добавление сигнатуры
    public SignatureEntity addSignature(SignatureEntity signature) {
        if (signature.getUpdatedAt() == null) {
            signature.setUpdatedAt(LocalDateTime.now());
        }
        if (signature.getStatus() == null) {
            signature.setStatus("ACTUAL");
        }
        SignatureEntity saved = signatureRepository.save(signature);

        // Запись в аудит
        SignatureAuditEntity audit = new SignatureAuditEntity();
        audit.setSignatureId(saved.getId());
        audit.setChangedBy("system");
        audit.setChangeType("CREATED");
        audit.setChangedAt(LocalDateTime.now());
        audit.setFieldsChanged("New signature added");
        auditRepository.save(audit);

        return saved;
    }

    public SignatureEntity updateSignature(Long id, SignatureEntity updatedSignature) {
        Optional<SignatureEntity> existingOpt = signatureRepository.findById(id);
        if (!existingOpt.isPresent()) {
            throw new RuntimeException("Signature not found");
        }

        SignatureEntity existing = existingOpt.get();
        if ("DELETED".equals(existing.getStatus())) {
            throw new RuntimeException("Cannot update DELETED signature");
        }

        // Сохраняем текущее состояние в историю
        SignatureHistoryEntity history = new SignatureHistoryEntity();
        history.setSignatureId(existing.getId());
        history.setVersionCreatedAt(LocalDateTime.now());
        history.setThreatName(existing.getThreatName());
        history.setFirstBytes(existing.getFirstBytes());
        history.setRemainderHash(existing.getRemainderHash());
        history.setRemainderLength(existing.getRemainderLength());
        history.setFileType(existing.getFileType());
        history.setOffsetStart(existing.getOffsetStart());
        history.setOffsetEnd(existing.getOffsetEnd());
        history.setDigitalSignature(existing.getDigitalSignature());
        history.setStatus(existing.getStatus());
        history.setUpdatedAt(existing.getUpdatedAt());
        historyRepository.save(history);

        // Формируем список изменённых полей
        List<String> fieldsChanged = new ArrayList<>();
        if (!Objects.equals(updatedSignature.getThreatName(), existing.getThreatName())) {
            fieldsChanged.add("threatName");
        }
        if (!Arrays.equals(updatedSignature.getFirstBytes(), existing.getFirstBytes())) {
            fieldsChanged.add("firstBytes");
        }
        if (!Objects.equals(updatedSignature.getRemainderHash(), existing.getRemainderHash())) {
            fieldsChanged.add("remainderHash");
        }
        if (updatedSignature.getRemainderLength() != existing.getRemainderLength()) {
            fieldsChanged.add("remainderLength");
        }
        if (!Objects.equals(updatedSignature.getFileType(), existing.getFileType())) {
            fieldsChanged.add("fileType");
        }
        if (updatedSignature.getOffsetStart() != existing.getOffsetStart()) {
            fieldsChanged.add("offsetStart");
        }
        if (updatedSignature.getOffsetEnd() != existing.getOffsetEnd()) {
            fieldsChanged.add("offsetEnd");
        }

        // Обновляем сигнатуру
        existing.setThreatName(updatedSignature.getThreatName());
        existing.setFirstBytes(updatedSignature.getFirstBytes()); // Теперь работает с byte[]
        existing.setRemainderHash(updatedSignature.getRemainderHash());
        existing.setRemainderLength(updatedSignature.getRemainderLength());
        existing.setFileType(updatedSignature.getFileType());
        existing.setOffsetStart(updatedSignature.getOffsetStart());
        existing.setOffsetEnd(updatedSignature.getOffsetEnd());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setStatus("ACTUAL");
        SignatureEntity updated = signatureRepository.save(existing);

        // Запись в аудит
        SignatureAuditEntity audit = new SignatureAuditEntity();
        audit.setSignatureId(updated.getId());
        audit.setChangedBy("system");
        audit.setChangeType("UPDATED");
        audit.setChangedAt(LocalDateTime.now());
        audit.setFieldsChanged(fieldsChanged.isEmpty() ? "No fields changed" : String.join(", ", fieldsChanged));
        auditRepository.save(audit);

        return updated;
    }

    // Удаление сигнатуры (установка статуса DELETED)
    public void deleteSignature(Long id) {
        Optional<SignatureEntity> existingOpt = signatureRepository.findById(id);
        if (!existingOpt.isPresent()) {
            throw new RuntimeException("Signature not found");
        }

        SignatureEntity existing = existingOpt.get();
        if ("DELETED".equals(existing.getStatus())) {
            throw new RuntimeException("Signature already deleted");
        }

        // Сохраняем текущее состояние в историю
        SignatureHistoryEntity history = new SignatureHistoryEntity();
        history.setSignatureId(existing.getId());
        history.setVersionCreatedAt(LocalDateTime.now());
        history.setThreatName(existing.getThreatName());
        history.setFirstBytes(existing.getFirstBytes());
        history.setRemainderHash(existing.getRemainderHash());
        history.setRemainderLength(existing.getRemainderLength());
        history.setFileType(existing.getFileType());
        history.setOffsetStart(existing.getOffsetStart());
        history.setOffsetEnd(existing.getOffsetEnd());
        history.setDigitalSignature(existing.getDigitalSignature());
        history.setStatus(existing.getStatus());
        history.setUpdatedAt(existing.getUpdatedAt());
        historyRepository.save(history);

        // Устанавливаем статус DELETED
        existing.setStatus("DELETED");
        existing.setUpdatedAt(LocalDateTime.now());
        signatureRepository.save(existing);

        // Запись в аудит
        SignatureAuditEntity audit = new SignatureAuditEntity();
        audit.setSignatureId(existing.getId());
        audit.setChangedBy("system");
        audit.setChangeType("DELETED");
        audit.setChangedAt(LocalDateTime.now());
        audit.setFieldsChanged("Status changed to DELETED");
        auditRepository.save(audit);
    }

    public List<SignatureEntity> getAllSignatures() {
        return signatureRepository.findByStatus("ACTUAL");
    }

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
            return false;
        }

        for (int i = 0; i < WINDOW_SIZE; i++) {
            if (window[i] != signatureFirstBytes[i]) {
                return false;
            }
        }

        long startOffset = offset;
        long endOffset = offset + WINDOW_SIZE + signature.getRemainderLength();
        if (startOffset < signature.getOffsetStart() || endOffset > signature.getOffsetEnd()) {
            return false;
        }

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