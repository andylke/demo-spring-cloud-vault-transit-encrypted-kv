package com.github.andylke.demo.vault;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultTransitOperations;
import org.springframework.vault.support.Ciphertext;
import org.springframework.vault.support.VaultDecryptionResult;

public class TransitEncryptedKeyValueDecrypter {

  private static final String TRANSIT_ENCRYPTED_PREFIX = "vault:v";

  private final TransitEncryptedKeyValueProperties properties;

  private final VaultTransitOperations transitOperations;

  public TransitEncryptedKeyValueDecrypter(
      TransitEncryptedKeyValueProperties properties, VaultTemplate vaultTemplate) {
    this.properties = properties;
    this.transitOperations = vaultTemplate.opsForTransit(properties.getTransitPath());
  }

  public void decryptAndPopulate(
      EnumerablePropertySource<?> propertySource, Properties properties) {

    final LinkedHashMap<String, String> encryptedKeyValues =
        filterEncryptedKeyValues(propertySource);
    if (encryptedKeyValues.isEmpty()) {
      properties.clear();
      return;
    }

    final List<String> decryptedValues = decryptValues(encryptedKeyValues);
    clearAndPopulateTo(properties, encryptedKeyValues, decryptedValues);
  }

  private LinkedHashMap<String, String> filterEncryptedKeyValues(
      EnumerablePropertySource<?> source) {
    final LinkedHashMap<String, String> keyValues = new LinkedHashMap<String, String>();

    for (String propertyName : source.getPropertyNames()) {
      final Object propertyValue = source.getProperty(propertyName);
      if (isTransitEncrypted(propertyValue)) {
        keyValues.put(propertyName, (String) propertyValue);
      }
    }

    return keyValues;
  }

  private boolean isTransitEncrypted(Object propertyValue) {
    return propertyValue instanceof String
        && StringUtils.startsWith((String) propertyValue, TRANSIT_ENCRYPTED_PREFIX);
  }

  private List<String> decryptValues(LinkedHashMap<String, String> encryptedKeyValues) {
    final List<VaultDecryptionResult> results =
        transitOperations.decrypt(
            properties.getTransitKeyName(),
            encryptedKeyValues
                .entrySet()
                .stream()
                .map(entry -> Ciphertext.of(entry.getValue()))
                .collect(Collectors.toList()));

    return results.stream().map(result -> result.getAsString()).collect(Collectors.toList());
  }

  private void clearAndPopulateTo(
      Properties properties,
      LinkedHashMap<String, String> encryptedKeyValues,
      List<String> decryptedValues) {
    properties.clear();

    int index = 0;
    for (Entry<String, String> entry : encryptedKeyValues.entrySet()) {
      properties.put(entry.getKey(), decryptedValues.get(index++));
    }
  }
}
