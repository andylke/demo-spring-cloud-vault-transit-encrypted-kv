package com.github.andylke.demo.vault;

import org.springframework.vault.core.env.LeaseAwareVaultPropertySource;

public class TransitDecryptedLeaseAwareVaultPropertySource
    extends TransitDecryptedPropertySource<LeaseAwareVaultPropertySource> {

  public TransitDecryptedLeaseAwareVaultPropertySource(
      LeaseAwareVaultPropertySource source, TransitEncryptedKeyValueDecrypter decrypter) {
    super(source, decrypter);
  }
}
