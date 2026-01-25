package com.fnl33.featuretoggle.service;

import com.fnl33.featuretoggle.domain.Attribute;
import com.fnl33.featuretoggle.domain.ClientRegistration;
import com.fnl33.featuretoggle.domain.Toggle;
import com.fnl33.featuretoggle.repository.ClientRegistrationRepository;
import com.fnl33.featuretoggle.service.event.AttributeChangedEvent;
import com.fnl33.featuretoggle.service.event.ToggleNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationOrchestrator {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void notifyToggleChange(Toggle toggle, String value) {
        final List<String> callbackUrls = clientRegistrationRepository.findByTogglesContains(toggle.getName()).stream()
                .map(ClientRegistration::getCallbackUrl)
                .toList();
        final ToggleNotificationEvent event = new ToggleNotificationEvent(toggle.getName(), toggle.isEnabled(), value, callbackUrls);
        publishAfterCommit(event);
    }

    public void notifyAttributeChange(Attribute attribute, List<String> affectedToggleNames) {
        final AttributeChangedEvent event = new AttributeChangedEvent(attribute.getName(), List.copyOf(affectedToggleNames));
        publishAfterCommit(event);
    }

    private void publishAfterCommit(Object event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventPublisher.publishEvent(event);
                }
            });
        } else {
            eventPublisher.publishEvent(event);
        }
    }
}
