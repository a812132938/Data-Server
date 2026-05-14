package com.isom.dataserver.module.api.spi;

import com.isom.dataserver.common.spi.SqlTemplateParser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MyBatisSqlTemplateParser implements SqlTemplateParser {

    private static final Pattern HASH_PATTERN = Pattern.compile("#\\{(\\w+)(?:,.*?)?\\}");
    private static final Pattern FOREACH_PATTERN = Pattern.compile("<foreach[^>]+collection\\s*=\\s*\"(\\w+)\"");

    @Override
    public List<String> parseParamNames(String sqlTemplate) {
        Set<String> names = new LinkedHashSet<>();

        Matcher m1 = HASH_PATTERN.matcher(sqlTemplate);
        while (m1.find()) {
            names.add(m1.group(1));
        }

        Matcher m2 = FOREACH_PATTERN.matcher(sqlTemplate);
        while (m2.find()) {
            names.add(m2.group(1));
        }

        return new ArrayList<>(names);
    }
}
