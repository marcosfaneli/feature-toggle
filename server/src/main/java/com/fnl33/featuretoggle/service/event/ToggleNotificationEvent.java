package com.fnl33.featuretoggle.service.event;

import java.util.List;

public record ToggleNotificationEvent(String toggleName, boolean enabled, String value, List<String> callbackUrls) {
}
