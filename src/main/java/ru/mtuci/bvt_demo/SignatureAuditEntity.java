package ru.mtuci.bvt_demo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "signature_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignatureAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    @Column(name = "signature_id")
    private Long signatureId;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "change_type")
    private String changeType;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "fields_changed")
    private String fieldsChanged;
}
