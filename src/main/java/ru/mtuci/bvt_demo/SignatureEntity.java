package ru.mtuci.bvt_demo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "signature_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignatureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "threat_name", nullable = false)
    private String threatName;

    @Column(name = "first_bytes", nullable = false)
    private byte[] firstBytes;

    @Column(name = "remainder_hash", nullable = false)
    private String remainderHash;

    @Column(name = "remainder_length", nullable = false)
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

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "status", nullable = false)
    private String status;
}