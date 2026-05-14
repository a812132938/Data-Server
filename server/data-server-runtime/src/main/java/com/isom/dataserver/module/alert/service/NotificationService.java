package com.isom.dataserver.module.alert.service;

import java.util.Map;

public interface NotificationService {
    Map<String, Object> insite(String status, String type, Integer limit);
    Map<String, Object> markRead(Long id);
    Map<String, Object> markAllRead();
}
