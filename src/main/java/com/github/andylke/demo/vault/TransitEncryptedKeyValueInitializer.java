package com.github.andylke.demo.vault;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.env.LeaseAwareVaultPropertySource;
import org.springframework.vault.core.env.VaultPropertySource;

public class TransitEncryptedKeyValueInitializer
    implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

  @Override
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    final TransitEncryptedKeyValueProperties properties =
        registerAndCreateProperties(event.getBootstrapContext());
    if (properties.isTransitEncrypted() == false) {
      return;
    }

    findAndCreateDecryptedPropertySource(
        event.getEnvironment().getPropertySources(),
        registerAndCreateDecrypter(event.getBootstrapContext(), properties));
  }

  private TransitEncryptedKeyValueProperties registerAndCreateProperties(
      ConfigurableBootstrapContext bootstrapContext) {

    bootstrapContext.register(
        TransitEncryptedKeyValueProperties.class,
        (context) -> {
          final Binder binder = context.get(Binder.class);

          final TransitEncryptedKeyValueProperties properties =
              binder.bindOrCreate(
                  TransitEncryptedKeyValueProperties.PREFIX,
                  TransitEncryptedKeyValueProperties.class);

          if (StringUtils.isBlank(properties.getTransitKeyName())) {
            properties.setTransitKeyName(
                binder
                    .bind("spring.cloud.vault.application-name", String.class)
                    .orElse(
                        binder
                            .bind("spring.application.name", String.class)
                            .orElse("application")));
          }
          return properties;
        });

    return bootstrapContext.get(TransitEncryptedKeyValueProperties.class);
  }

  private TransitEncryptedKeyValueDecrypter registerAndCreateDecrypter(
      ConfigurableBootstrapContext bootstrapContext,
      TransitEncryptedKeyValueProperties properties) {

    bootstrapContext.register(
        TransitEncryptedKeyValueDecrypter.class,
        (context) -> {
          return new TransitEncryptedKeyValueDecrypter(
              properties, context.get(VaultTemplate.class));
        });

    return bootstrapContext.get(TransitEncryptedKeyValueDecrypter.class);
  }

  private void findAndCreateDecryptedPropertySource(
      MutablePropertySources propertySources, TransitEncryptedKeyValueDecrypter decrypter) {
    final List<TransitDecryptedPropertySource<?>> decryptedPropertySources = new ArrayList<>();

    for (PropertySource<?> propertySource : propertySources) {
      if (propertySource instanceof LeaseAwareVaultPropertySource) {
        decryptedPropertySources.add(
            new TransitDecryptedLeaseAwareVaultPropertySource(
                (LeaseAwareVaultPropertySource) propertySource, decrypter));
      }
      if (propertySource instanceof VaultPropertySource) {
        decryptedPropertySources.add(
            new TransitDecryptedVaultPropertySource(
                (VaultPropertySource) propertySource, decrypter));
      }
    }

    decryptedPropertySources.forEach(
        decryptedPropertySource ->
            propertySources.addBefore(
                decryptedPropertySource.getSource().getName(), decryptedPropertySource));
  }
}
