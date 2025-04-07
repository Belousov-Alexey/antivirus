package ru.mtuci.bvt_demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignatureScanResult {
    private Long signatureId;
    private String threatName;
    private long offsetFromStart;
    private long offsetFromEnd;
    private boolean matched;
}
