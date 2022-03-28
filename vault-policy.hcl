# Read secrets
path "secret/*" {
  capabilities = ["read"]
}

# Read Transit keys
path "transit/keys/*" {
  capabilities = ["read"]
}
