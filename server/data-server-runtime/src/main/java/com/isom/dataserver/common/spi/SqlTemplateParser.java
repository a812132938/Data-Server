package com.isom.dataserver.common.spi;

import java.util.List;

public interface SqlTemplateParser {
    List<String> parseParamNames(String sqlTemplate);
}
