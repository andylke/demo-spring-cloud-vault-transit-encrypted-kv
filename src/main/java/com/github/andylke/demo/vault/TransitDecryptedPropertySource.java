package com.github.andylke.demo.vault;

import java.util.Properties;

import org.springframework.core.env.EnumerablePropertySource;

public abstract class TransitDecryptedPropertySource<T extends EnumerablePropertySource<?>>
    extends EnumerablePropertySource<T> {

  private final Properties properties = new Properties();

  private final TransitEncryptedKeyValueDecrypter decrypter;

  protected TransitDecryptedPropertySource(T source, TransitEncryptedKeyValueDecrypter decrypter) {
    super(addPrefix(source.getName()), source);

    this.decrypter = decrypter;
    decryptAndPopulate();
  }

  public static String addPrefix(String name) {
    return "transit-decrypted:" + name;
  }

  @Override
  public Object getProperty(String name) {
    return properties.get(name);
  }

  @Override
  public String[] getPropertyNames() {
    return properties.stringPropertyNames().toArray(new String[properties.size()]);
  }

  public void decryptAndPopulate() {
    decrypter.decryptAndPopulate(source, properties);
  }
}
