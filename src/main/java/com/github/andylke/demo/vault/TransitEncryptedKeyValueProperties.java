package com.github.andylke.demo.vault;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for Vault using transit encrypted key-value backed. */
@ConfigurationProperties(prefix = TransitEncryptedKeyValueProperties.PREFIX)
public class TransitEncryptedKeyValueProperties {

  public static final String PREFIX = "spring.cloud.vault.kv";

  private boolean transitEncrypted = false;

  private String transitKeyName;

  private String transitPath = "transit";

  private Map<String, String> postDecryptMappings = new HashMap<String, String>();

  public boolean isTransitEncrypted() {
    return transitEncrypted;
  }

  public void setTransitEncrypted(boolean transitEncrypted) {
    this.transitEncrypted = transitEncrypted;
  }

  public String getTransitKeyName() {
    return transitKeyName;
  }

  public void setTransitKeyName(String transitKeyName) {
    this.transitKeyName = transitKeyName;
  }

  public String getTransitPath() {
    return transitPath;
  }

  public void setTransitPath(String transitPath) {
    this.transitPath = transitPath;
  }

  public Map<String, String> getPostDecryptMappings() {
    return postDecryptMappings;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
  }
}
