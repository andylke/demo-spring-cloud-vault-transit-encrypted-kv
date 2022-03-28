package com.github.andylke.demo.vault;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.vault.config.VaultKeyValueBackendProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.vault.core.lease.SecretLeaseContainer;
import org.springframework.vault.core.lease.event.LeaseListener;
import org.springframework.vault.core.lease.event.SecretLeaseCreatedEvent;
import org.springframework.vault.core.lease.event.SecretLeaseEvent;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
    prefix = TransitEncryptedKeyValueProperties.PREFIX,
    name = "transit-encrypted",
    havingValue = "true",
    matchIfMissing = false)
@EnableConfigurationProperties({
  VaultKeyValueBackendProperties.class,
  TransitEncryptedKeyValueProperties.class
})
public class TransitEncryptedKeyValueConfiguration {

  @Autowired private List<SecretLeaseContainer> secretLeaseContainers;

  @Autowired private ConfigurableEnvironment environment;

  @PostConstruct
  void postConstruct() {
    secretLeaseContainers
        .stream()
        .forEach((secretLeaseContainer) -> addLeaseListener(secretLeaseContainer));
  }

  private void addLeaseListener(SecretLeaseContainer secretLeaseContainer) {
    secretLeaseContainer.addLeaseListener(
        new LeaseListener() {

          @Override
          public void onLeaseEvent(SecretLeaseEvent leaseEvent) {
            if (leaseEvent instanceof SecretLeaseCreatedEvent) {
              final TransitDecryptedLeaseAwareVaultPropertySource decryptedPropertySource =
                  (TransitDecryptedLeaseAwareVaultPropertySource)
                      environment
                          .getPropertySources()
                          .get(
                              TransitDecryptedPropertySource.addPrefix(
                                  leaseEvent.getSource().getPath()));
              if (decryptedPropertySource != null) {
                decryptedPropertySource.decryptAndPopulate();
              }
            }
          }
        });
  }
}
