package ru.mtuci.bvt_demo;

import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Getter
@Setter
public class SignatureDTO {
    private String threatName;
    private String firstBytes; // Строка, будет преобразована в byte[]
    private String remainderHash;
    private int remainderLength;
    private String fileType;
    private int offsetStart;
    private int offsetEnd;
    private LocalDateTime updatedAt;
    private String status;

    // Конвертация в SignatureEntity
    public SignatureEntity toEntity() {
        SignatureEntity entity = new SignatureEntity();
        entity.setThreatName(threatName);
        entity.setFirstBytes(firstBytes != null ? firstBytes.getBytes(StandardCharsets.UTF_8) : null);
        entity.setRemainderHash(remainderHash);
        entity.setRemainderLength(remainderLength);
        entity.setFileType(fileType);
        entity.setOffsetStart(offsetStart);
        entity.setOffsetEnd(offsetEnd);
        entity.setUpdatedAt(updatedAt);
        entity.setStatus(status);
        return entity;
    }
}