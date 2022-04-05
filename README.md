# Demo Spring Cloud Vault

## Terminal 1

### Start Server

`vault server -config vault-server.hcl`

```
==> Vault server configuration:

                     Cgo: disabled
              Go Version: go1.17.7
              Listener 1: tcp (addr: "127.0.0.1:8200", cluster address: "127.0.0.1:8201", max_request_duration: "1m30s", max_request_size: "33554432", tls: "disabled")
               Log Level: info
                   Mlock: supported: false, enabled: false
           Recovery Mode: false
                 Storage: file
                 Version: Vault v1.9.4
             Version Sha: fcbe948b2542a13ee8036ad07dd8ebf8554f56cb

==> Vault server started! Log data will stream in below:

2022-03-22T21:10:11.886+0800 [INFO]  proxy environment: http_proxy="" https_proxy="" no_proxy=""
2022-03-22T21:10:11.944+0800 [WARN]  no `api_addr` value specified in config or in VAULT_API_ADDR; falling back to detection if possible, but this value should be manually set
```

## Terminal 2

### Init

`vault operator init`

```
Unseal Key 1: ltLupdgLm/zwAHLxKWYMnncMuezUNE98j+Th4cEdvMk3
Unseal Key 2: fWW1V7CzEGjDzEjW2PZ1jshVP3BnXtyuhyUYN+CR3Yp6
Unseal Key 3: 1L8Dfv1WyKH62D9o3n6ycsL0g9GLAfle0OebTUbgioZP
Unseal Key 4: Fe4slrDLbkFSzs4ZjwolSO0bdAEGfQ21ZhfMp/pQhD2l
Unseal Key 5: tGriR0LRL4PJJKUCzFNi373qe4hKQNXdUdcyWMpP306p

Initial Root Token: s.9DnSsGSceorKLGtD7EINh18M

Vault initialized with 5 key shares and a key threshold of 3. Please securely
distribute the key shares printed above. When the Vault is re-sealed,
restarted, or stopped, you must supply at least 3 of these keys to unseal it
before it can start servicing requests.

Vault does not store the generated master key. Without at least 3 keys to
reconstruct the master key, Vault will remain permanently sealed!

It is possible to generate new unseal keys, provided you have a quorum of
existing unseal keys shares. See "vault operator rekey" for more information.
```

###

`export VAULT_TOKEN=s.9DnSsGSceorKLGtD7EINh18M`

`vault status`

```
Key                Value
---                -----
Seal Type          shamir
Initialized        true
Sealed             false
Total Shares       5
Threshold          3
Unseal Progress    0/3
Unseal Nonce       bc94403e-2af2-8256-ca9d-b3b80a6e1168
Version            1.9.4
Storage Type       file
HA Enabled         false
```

### Unseal

`vault operator unseal`

```
Key             Value
---             -----
Seal Type       shamir
Initialized     true
Sealed          false
Total Shares    5
Threshold       3
Version         1.9.4
Storage Type    file
Cluster Name    vault-cluster-739cb75a
Cluster ID      dd510672-e9a0-e7c7-f766-c4b09525328a
HA Enabled      false
```

### Enable Secrets

`vault secrets enable -path=secret/ kv`

```
Success! Enabled the kv secrets engine at: secret/
```

### Enable Transit

`vault secrets enable transit`

```
Success! Enabled the transit secrets engine at: transit/
```

### Create Transit Encryption Key

`vault write -f transit/keys/spring-cloud-vault`

```
Success! Data written to: transit/keys/spring-cloud-vault
```

### Get Base64 String

`echo -n demo_spring_cloud_vault_transit_encrypted_kv | base64`

| Key                 | Text                                         | Base64                                                       |
| ------------------- | -------------------------------------------- | ------------------------------------------------------------ |
| h2database.name     | demo_spring_cloud_vault_transit_encrypted_kv | ZGVtb19zcHJpbmdfY2xvdWRfdmF1bHRfdHJhbnNpdF9lbmNyeXB0ZWRfa3Y= |
| h2database.username | demo                                         | ZGVtbw==                                                     |
| h2database.password | demo@123                                     | ZGVtb0AxMjM=                                                 |

### Encrypt

`vault write transit/encrypt/spring-cloud-vault plaintext=ZGVtb19zcHJpbmdfY2xvdWRfdmF1bHRfdHJhbnNpdF9lbmNyeXB0ZWRfa3Y=`

```
Key            Value
---            -----
ciphertext     vault:v1:fuEuSZZ5JARE6GrZSrfP550K/m1ekOgqSHlfyeRPFvL1QkT3a0GLKvu7gm+hbdlXfkWBXhaOGLfe3sbKhGNJqceZvgbSDtb1
key_version    1
```

`vault write transit/encrypt/spring-cloud-vault plaintext=ZGVtbw==`

```
Key            Value
---            -----
ciphertext     vault:v1:pYM7KeN11NlcmSDc1kKRwPXbiL7uDdQ7Dg4mljdThfY=
key_version    1
```

`vault write transit/encrypt/spring-cloud-vault plaintext=ZGVtb0AxMjM=`

```
Key            Value
---            -----
ciphertext     vault:v1:tKoHqzhbWzw02C1yRwRkFBC1mh7gHoBGCzR2kTU+25JWSeqw
key_version    1
```

| Key         | Text                                         | Base64                                                       | Transit Encrypted                                                                                   |
| ----------- | -------------------------------------------- | ------------------------------------------------------------ | --------------------------------------------------------------------------------------------------- |
| db.name     | demo_spring_cloud_vault_transit_encrypted_kv | ZGVtb19zcHJpbmdfY2xvdWRfdmF1bHRfdHJhbnNpdF9lbmNyeXB0ZWRfa3Y= | v1:fuEuSZZ5JARE6GrZSrfP550K/m1ekOgqSHlfyeRPFvL1QkT3a0GLKvu7gm+hbdlXfkWBXhaOGLfe3sbKhGNJqceZvgbSDtb1 |
| db.username | demo                                         | ZGVtbw==                                                     | vault:v1:pYM7KeN11NlcmSDc1kKRwPXbiL7uDdQ7Dg4mljdThfY=                                               |
| db.password | demo@123                                     | ZGVtb0AxMjM=                                                 | vault:v1:tKoHqzhbWzw02C1yRwRkFBC1mh7gHoBGCzR2kTU+25JWSeqw                                           |

### Store secrets

```
vault kv put secret/spring-cloud-vault/demo \
    db.name=vault:v1:fuEuSZZ5JARE6GrZSrfP550K/m1ekOgqSHlfyeRPFvL1QkT3a0GLKvu7gm+hbdlXfkWBXhaOGLfe3sbKhGNJqceZvgbSDtb1 \
    db.username=vault:v1:pYM7KeN11NlcmSDc1kKRwPXbiL7uDdQ7Dg4mljdThfY= \
    db.password=vault:v1:tKoHqzhbWzw02C1yRwRkFBC1mh7gHoBGCzR2kTU+25JWSeqw
```

```
Success! Data written to: secret/spring-cloud-vault/demo
```

### Retrieve secrets

`vault kv get secret/spring-cloud-vault/demo`

```
======= Data =======
Key            Value
---            -----
db.name        vault:v1:fuEuSZZ5JARE6GrZSrfP550K/m1ekOgqSHlfyeRPFvL1QkT3a0GLKvu7gm+hbdlXfkWBXhaOGLfe3sbKhGNJqceZvgbSDtb1
db.password    vault:v1:tKoHqzhbWzw02C1yRwRkFBC1mh7gHoBGCzR2kTU+25JWSeqw
db.username    vault:v1:pYM7KeN11NlcmSDc1kKRwPXbiL7uDdQ7Dg4mljdThfY=
```

## Application Properties

`application.yaml`

```
spring:
  application:
    name: demo-spring-cloud-vault
  profiles:
    active: demo

  datasource:
    url: jdbc:h2:mem:${db.name}
    username: ${db.username}
    password: ${db.password}
  h2:
    console:
      enabled: true

  config:
    import: vault://
  cloud:
    vault:
      enabled: true
      application-name: spring-cloud-vault
      host: localhost
      port: 8200
      scheme: http
      token: s.FZTp0c0fJV1D05D1qYDZsAtM
```

```
/secret/{application}/{profile}
/secret/{application}
/secret/{defaultContext}/{profile}
/secret/{defaultContext}
/secret/application/{profile}
/secret/application
```

| Arguments        | Source Property                                               |
| ---------------- | ------------------------------------------------------------- |
| application-name | spring.cloud.vault.application-name / spring.application.name |
| profile          | spring.profiles.active                                        |
