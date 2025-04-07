package ru.mtuci.bvt_demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanMatch {
    private long startOffset;
    private long endOffset;
}
