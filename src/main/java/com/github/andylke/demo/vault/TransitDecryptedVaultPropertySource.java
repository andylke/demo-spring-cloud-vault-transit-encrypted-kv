package com.github.andylke.demo.vault;

import org.springframework.vault.core.env.VaultPropertySource;

public class TransitDecryptedVaultPropertySource
    extends TransitDecryptedPropertySource<VaultPropertySource> {

  public TransitDecryptedVaultPropertySource(
      VaultPropertySource source, TransitEncryptedKeyValueDecrypter decrypter) {
    super(source, decrypter);
  }
}
