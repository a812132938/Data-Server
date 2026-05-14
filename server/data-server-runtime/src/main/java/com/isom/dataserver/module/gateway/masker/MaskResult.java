package com.isom.dataserver.module.gateway.masker;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MaskResult {
    private List<Map<String, Object>> rows;
    private boolean masked;
}
