package com.isom.dataserver.module.gateway.masker;

import com.isom.dataserver.module.api.entity.ApiResponseField;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataMasker {

    public MaskResult mask(List<Map<String, Object>> rows, List<ApiResponseField> fields) {
        MaskResult result = new MaskResult();
        if (fields == null || fields.isEmpty() || rows == null || rows.isEmpty()) {
            result.setRows(rows);
            result.setMasked(false);
            return result;
        }

        Map<String, ApiResponseField> sensitiveFields = new HashMap<>();
        for (ApiResponseField f : fields) {
            if (f.getSensitive() != null && f.getSensitive() == 1) {
                String key = f.getAlias() != null && !f.getAlias().isEmpty() ? f.getAlias() : f.getFieldName();
                sensitiveFields.put(key, f);
            }
        }

        if (sensitiveFields.isEmpty()) {
            result.setRows(rows);
            result.setMasked(false);
            return result;
        }

        List<Map<String, Object>> maskedRows = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> maskedRow = new LinkedHashMap<>(row);
            for (Map.Entry<String, ApiResponseField> entry : sensitiveFields.entrySet()) {
                if (maskedRow.containsKey(entry.getKey())) {
                    Object val = maskedRow.get(entry.getKey());
                    if (val != null) {
                        maskedRow.put(entry.getKey(), applyMask(val.toString(), entry.getValue().getSensitiveRule()));
                    }
                }
            }
            maskedRows.add(maskedRow);
        }

        result.setRows(maskedRows);
        result.setMasked(true);
        return result;
    }

    private String applyMask(String value, String rule) {
        if (rule == null) rule = "CUSTOM";
        switch (rule.toUpperCase()) {
            case "EMAIL":
                int at = value.indexOf('@');
                if (at > 1) return value.charAt(0) + "***" + value.substring(at);
                return "***";
            case "PHONE":
                if (value.length() >= 7) return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
                return "****";
            case "ID_CARD":
                if (value.length() >= 8) return value.substring(0, 4) + "**********" + value.substring(value.length() - 4);
                return "****";
            default:
                if (value.length() <= 2) return "**";
                return value.charAt(0) + "***" + value.charAt(value.length() - 1);
        }
    }
}
