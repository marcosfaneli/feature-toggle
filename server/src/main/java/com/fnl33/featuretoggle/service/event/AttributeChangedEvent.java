package com.fnl33.featuretoggle.service.event;

import java.util.List;

public record AttributeChangedEvent(String attributeName, List<String> affectedToggleNames) {
}
