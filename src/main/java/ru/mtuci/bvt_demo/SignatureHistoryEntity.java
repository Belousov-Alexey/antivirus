package ru.mtuci.bvt_demo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "signature_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignatureHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @Column(name = "signature_id")
    private Long signatureId;

    @Column(name = "version_created_at", nullable = false)
    private LocalDateTime versionCreatedAt;

    @Column(name = "threat_name")
    private String threatName;

    @Column(name = "first_bytes")
    private byte[] firstBytes;

    @Column(name = "remainder_hash")
    private String remainderHash;

    @Column(name = "remainder_length")
    private int remainderLength;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "offset_start")
    private int offsetStart;

    @Column(name = "offset_end")
    private int offsetEnd;

    @Lob
    @Column(name = "digital_signature")
    private byte[] digitalSignature;

    @Column(name = "status")
    private String status;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
