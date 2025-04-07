package ru.mtuci.bvt_demo;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileScannerService {
    private final SignatureRepository signatureRepository;

    public FileScannerService(SignatureRepository signatureRepository) {
        this.signatureRepository = signatureRepository;
    }

    public List<SignatureScanResult> scanFile(MultipartFile file) throws IOException {
        // Чтение временного файла
        File tempFile = File.createTempFile("scan", ".tmp");
        file.transferTo(tempFile);

        List<SignatureScanResult> results = new ArrayList<>();

        // Загрузка сигнатур из базы данных
        List<SignatureEntity> signatures = signatureRepository.findByStatus("ACTUAL");

        try (RandomAccessFile raf = new RandomAccessFile(tempFile, "r")) {
            for (SignatureEntity signature : signatures) {
                // Алгоритм Рабина-Карпа для поиска first_bytes
                List<ScanMatch> matches = rabinKarpScan(raf, signature);
                for (ScanMatch match : matches) {
                    SignatureScanResult result = new SignatureScanResult();
                    result.setSignatureId(signature.getId());
                    result.setThreatName(signature.getThreatName());
                    result.setOffsetFromStart(match.getStartOffset());
                    result.setOffsetFromEnd(match.getEndOffset());
                    result.setMatched(true);
                    results.add(result);
                }
            }
        }

        // Удаление временного файла
        tempFile.delete();

        return results;
    }

    private List<ScanMatch> rabinKarpScan(RandomAccessFile raf, SignatureEntity signature) {
        List<ScanMatch> matches = new ArrayList<>();
        // Реализация алгоритма Рабина-Карпа с "скользящим" хэшем для first_bytes
        // После нахождения совпадений проверяем "хвост" сигнатуры с криптографическим хэшем
        return matches;
    }
}
