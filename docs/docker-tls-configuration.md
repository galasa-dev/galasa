# Configuring Docker TLS Certificates for Galasa

## Introduction

This guide explains how to configure Galasa to connect to Docker engines that are protected with TLS (Transport Layer Security) certificates. Docker daemons can be configured to require client certificate authentication, which provides secure communication between Galasa and the Docker engine.

### What You'll Learn

- How to create a Certificate Authority (CA) for signing certificates
- How to generate server certificates for your Docker daemon
- How to generate client certificates for Galasa
- How to configure Docker to use TLS
- How to store certificates in Galasa's Credentials Store
- How to configure Galasa to use the certificates

### Prerequisites

- OpenSSL installed on your system
- Docker installed and running
- Java keytool (included with JDK)
- Access to modify Docker daemon configuration
- Galasa framework installed

---

## Step 1: Create a Certificate Authority (CA)

The Certificate Authority is used to sign both the server and client certificates, establishing trust between Docker and Galasa.

### 1.1 Generate CA Private Key

```bash
# Create a directory for certificates
mkdir -p ~/docker-certs
cd ~/docker-certs

# Generate CA private key (4096-bit RSA)
openssl genrsa -out ca-key.pem 4096
```

**Important:** Keep `ca-key.pem` secure! Anyone with this key can create trusted certificates.

### 1.2 Generate CA Certificate

```bash
# Create CA certificate (valid for 10 years)
openssl req -new -x509 -days 3650 -key ca-key.pem -sha256 -out ca.pem \
  -subj "/C=US/ST=State/L=City/O=Organization/OU=IT/CN=Docker-CA"
```

---

## Step 2: Create Server Certificates for Docker Daemon

The Docker daemon needs a server certificate to prove its identity to clients.

### 2.1 Generate Server Private Key

```bash
openssl genrsa -out server-key.pem 4096
```

### 2.2 Create Certificate Signing Request (CSR)

Replace `$DOCKER_HOST` with your Docker host's IP address or hostname:

```bash
# Set your Docker host IP/hostname
DOCKER_HOST="192.168.1.100"

# Create CSR
openssl req -subj "/CN=$DOCKER_HOST" -sha256 -new -key server-key.pem -out server.csr
```

### 2.3 Configure Subject Alternative Names (SAN)

Create a file named `extfile.cnf` with the following content (replace IP addresses as needed):

```bash
cat > extfile.cnf <<EOF
subjectAltName = DNS:$DOCKER_HOST,IP:$DOCKER_HOST,IP:127.0.0.1
extendedKeyUsage = serverAuth
EOF
```

### 2.4 Sign the Server Certificate

```bash
openssl x509 -req -days 365 -sha256 \
  -in server.csr \
  -CA ca.pem \
  -CAkey ca-key.pem \
  -CAcreateserial \
  -out server-cert.pem \
  -extfile extfile.cnf
```

<!-- ### 2.5 Set Proper Permissions

```bash
chmod 0400 ca-key.pem server-key.pem
chmod 0444 ca.pem server-cert.pem
``` -->

---

## Step 3: Create Client Certificates for Galasa

Galasa needs client certificates to authenticate to the Docker daemon.

### 3.1 Generate Client Private Key

```bash
openssl genrsa -out key.pem 4096
```

### 3.2 Create Client CSR

```bash
openssl req -subj '/CN=galasa-client' -new -key key.pem -out client.csr
```

### 3.3 Configure Client Extensions

Create `extfile-client.cnf`:

```bash
cat > extfile-client.cnf <<EOF
extendedKeyUsage = clientAuth
EOF
```

### 3.4 Sign the Client Certificate

```bash
openssl x509 -req -days 365 -sha256 \
  -in client.csr \
  -CA ca.pem \
  -CAkey ca-key.pem \
  -CAcreateserial \
  -out cert.pem \
  -extfile extfile-client.cnf
```

<!-- ### 3.5 Set Client Certificate Permissions

```bash
chmod 0400 key.pem
chmod 0444 cert.pem
``` -->

### 3.6 Verify Certificate Chain

```bash
# Verify server certificate
openssl verify -CAfile ca.pem server-cert.pem

# Verify client certificate
openssl verify -CAfile ca.pem cert.pem
```

Both should output: `OK`

---

## Step 4: Configure Docker Daemon

### 4.1 Copy Server Certificates to Docker

```bash
# Create Docker certificate directory
sudo mkdir -p /etc/docker/certs

# Copy server certificates
sudo cp ca.pem /etc/docker/certs/
sudo cp server-cert.pem /etc/docker/certs/
sudo cp server-key.pem /etc/docker/certs/

# Set ownership
sudo chown root:root /etc/docker/certs/*
sudo chmod 0400 /etc/docker/certs/server-key.pem
sudo chmod 0444 /etc/docker/certs/ca.pem /etc/docker/certs/server-cert.pem
```

### 4.2 Configure Docker Daemon

Edit `/etc/docker/daemon.json` (create if it doesn't exist):

```json
{
  "hosts": ["unix:///var/run/docker.sock", "tcp://0.0.0.0:2376"],
  "tls": true,
  "tlsverify": true,
  "tlscacert": "/etc/docker/certs/ca.pem",
  "tlscert": "/etc/docker/certs/server-cert.pem",
  "tlskey": "/etc/docker/certs/server-key.pem"
}
```

**Note:** Port 2376 is the standard Docker TLS port. Port 2375 is for unencrypted connections (not recommended).

### 4.3 Restart Docker Daemon

```bash
# Systemd-based systems
sudo systemctl restart docker

# Verify Docker is running with TLS
sudo systemctl status docker
```

### 4.4 Test Docker TLS Connection

```bash
# Test with curl (should fail without certificates)
curl https://$DOCKER_HOST:2376/version

# Test with certificates
curl --cacert ca.pem \
     --cert cert.pem \
     --key key.pem \
     https://$DOCKER_HOST:2376/version
```

You should see Docker version information in JSON format.

---

## Step 5: Create PKCS12 KeyStore for Galasa

Galasa stores certificates in a Java KeyStore format. We'll create a PKCS12 KeyStore containing both the client certificate and CA certificate.

### 5.1 Create KeyStore with Client Certificate

```bash
# Convert client cert and key to PKCS12 format
openssl pkcs12 -export \
  -in cert.pem \
  -inkey key.pem \
  -out client-only.p12 \
  -name "docker-client" \
  -password pass:changeit
```

**Important:** Replace `changeit` with a strong password!

### 5.2 Import into Final KeyStore

```bash
# Import the client certificate
keytool -importkeystore \
  -srckeystore client-only.p12 \
  -srcstoretype PKCS12 \
  -srcstorepass changeit \
  -destkeystore docker-tls.p12 \
  -deststoretype PKCS12 \
  -deststorepass changeit
```

### 5.3 Import CA Certificate

```bash
# Import CA certificate as trusted cert
keytool -importcert \
  -alias docker-ca \
  -file ca.pem \
  -keystore docker-tls.p12 \
  -storepass changeit \
  -storetype PKCS12 \
  -noprompt
```

### 5.4 Verify KeyStore Contents

```bash
keytool -list -v -keystore docker-tls.p12 -storepass changeit -storetype PKCS12
```

You should see:
- **Entry 1:** `docker-client` (PrivateKeyEntry) - Your client certificate and private key
- **Entry 2:** `docker-ca` (trustedCertEntry) - The CA certificate

---
---

## Alternative: Create JKS KeyStore for Galasa

While PKCS12 is the recommended format, you can also use the legacy JKS (Java KeyStore) format. Note that JKS is deprecated in newer Java versions, but is still supported by Galasa.

### Why Choose JKS?

- **Legacy compatibility:** Required for older Java applications
- **Existing infrastructure:** You may already have JKS keystores
- **Organizational standards:** Some organizations mandate JKS format

### Why PKCS12 is Recommended

- **Industry standard:** PKCS12 is the modern, cross-platform standard
- **Better security:** Stronger encryption algorithms
- **Future-proof:** JKS is deprecated in Java 9+
- **Wider support:** Works with non-Java tools

### Creating a JKS KeyStore

#### Option A: Create JKS Directly from Certificates

```bash
# Step 1: Convert client certificate and key to PKCS12 first
openssl pkcs12 -export \
  -in cert.pem \
  -inkey key.pem \
  -out temp-client.p12 \
  -name "docker-client" \
  -password pass:changeit

# Step 2: Convert PKCS12 to JKS
keytool -importkeystore \
  -srckeystore temp-client.p12 \
  -srcstoretype PKCS12 \
  -srcstorepass changeit \
  -destkeystore docker-tls.jks \
  -deststoretype JKS \
  -deststorepass changeit

# Step 3: Import CA certificate
keytool -importcert \
  -alias docker-ca \
  -file ca.pem \
  -keystore docker-tls.jks \
  -storepass changeit \
  -storetype JKS \
  -noprompt

# Step 4: Clean up temporary file
rm temp-client.p12
```

#### Option B: Convert Existing PKCS12 to JKS

If you already have a PKCS12 KeyStore from Step 5:

```bash
# Convert PKCS12 to JKS
keytool -importkeystore \
  -srckeystore docker-tls.p12 \
  -srcstoretype PKCS12 \
  -srcstorepass changeit \
  -destkeystore docker-tls.jks \
  -deststoretype JKS \
  -deststorepass changeit
```

### Verify JKS KeyStore

```bash
# List contents
keytool -list -v -keystore docker-tls.jks -storepass changeit -storetype JKS

# Expected output should show:
# - docker-client (PrivateKeyEntry)
# - docker-ca (trustedCertEntry)
```

### Using JKS with Galasa

When using JKS format, you'll need to:

1. **Base64 encode the JKS file** (see Step 6 below)
2. **Set the KeyStore type to JKS** in Galasa configuration:

```properties
secure.credentials.DOCKER_TLS.keystore=base64:MIIKEQIBAzCCCdcGCSqGSIb3DQE...
secure.credentials.DOCKER_TLS.password=changeit
secure.credentials.DOCKER_TLS.type=JKS
```

**Important:** Set `type=JKS` instead of `type=PKCS12`

### JKS Limitations

Be aware of these JKS limitations:

1. **Deprecated:** JKS is deprecated since Java 9
2. **Weaker encryption:** Uses older encryption algorithms
3. **Limited compatibility:** Not supported by non-Java tools
4. **No secret keys:** JKS cannot store symmetric keys (only certificates and private keys)

### Migration from JKS to PKCS12

If you need to migrate from JKS to PKCS12:

```bash
# Convert JKS to PKCS12
keytool -importkeystore \
  -srckeystore docker-tls.jks \
  -srcstoretype JKS \
  -srcstorepass changeit \
  -destkeystore docker-tls.p12 \
  -deststoretype PKCS12 \
  -deststorepass changeit

# Update Galasa configuration
# Change type=JKS to type=PKCS12
# Re-encode the PKCS12 file to base64 (see Step 6)
```

### JKS-Specific Troubleshooting

#### Issue: "JKS keystore uses a proprietary format"

**Cause:** Warning message from Java 9+ about JKS being deprecated.

**Solution:** This is just a warning. The KeyStore will still work, but consider migrating to PKCS12.

#### Issue: "Cannot store non-PrivateKeys"

**Cause:** Attempting to store secret keys in JKS format.

**Solution:** JKS only supports private keys and certificates. Use PKCS12 if you need to store secret keys.

#### Issue: "Keystore was tampered with, or password was incorrect"

**Cause:** Incorrect password or corrupted JKS file.

**Solution:**
```bash
# Verify password
keytool -list -keystore docker-tls.jks -storepass YOUR_PASSWORD

# If corrupted, recreate from original certificates
```

---


## Step 6: Base64 Encode the KeyStore

Galasa stores KeyStore data as base64-encoded text in the Credentials Store.

### 6.1 Encode the KeyStore

```bash
# Create base64-encoded version
base64 docker-tls.p12 > docker-tls.b64

# Verify encoding (should show base64 text)
head -n 5 docker-tls.b64
```

### 6.2 Create Single-Line Base64 (Optional)

For easier copying:

```bash
# Create single-line version
base64 docker-tls.p12 | tr -d '\n' > docker-tls-oneline.b64
```

---

## Step 7: Configure Galasa

### 7.1 Store Credentials in Galasa

Galasa provides two methods for storing KeyStore credentials:

#### Option A: File-Based Credentials Store

Edit your `~/.galasa/cps.properties` file:

```properties
# Docker TLS credentials
secure.credentials.DOCKER_TLS.keystore=base64:MIIKEQIBAzCCCdcGCSqGSIb3DQE...
secure.credentials.DOCKER_TLS.password=changeit
secure.credentials.DOCKER_TLS.type=PKCS12
```

**To get the base64 content:**
```bash
echo "base64:$(cat docker-tls-oneline.b64)"
```

#### Option B: ETCD Credentials Store

For Galasa services, you can use the Secrets REST API to manage KeyStore credentials:

```bash
# Prepare the secret payload
cat > docker-tls-secret.json <<EOF
{
  "name": "DOCKER_TLS",
  "description": "Docker TLS certificates for PRIMARY engine",
  "type": "KeyStore",
  "data": {
    "keystore": "base64:$(cat docker-tls-oneline.b64)",
    "password": "changeit",
    "type": "PKCS12"
  },
  "encoding": "base64"
}
EOF

# Create the secret using the API
curl -X POST https://your-galasa-api/secrets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d @docker-tls-secret.json

# Or update an existing secret
curl -X PUT https://your-galasa-api/secrets/DOCKER_TLS \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d @docker-tls-secret.json

# Retrieve the secret (values will be redacted unless you have permission)
curl -X GET https://your-galasa-api/secrets/DOCKER_TLS \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Delete the secret if needed
curl -X DELETE https://your-galasa-api/secrets/DOCKER_TLS \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Important Notes for Secrets API:**
- The KeyStore data must be base64-encoded and prefixed with "base64:" in the JSON payload
- The `encoding` field must be set to `"base64"`
- The `type` field in `data` specifies the KeyStore format (PKCS12 or JKS)
- Authentication is required via JWT token
- **Eager validation:** The KeyStore is validated when created/updated - you'll get immediate feedback if:
  - The KeyStore data is corrupted or invalid
  - The password is incorrect
  - The KeyStore type doesn't match the actual format
  - The KeyStore cannot be loaded
  - The "base64:" prefix is missing

**Secrets API Benefits:**
- ✅ **Fail-fast validation:** Errors detected immediately at creation time, not during test execution
- ✅ **Centralized management:** Single API for all credential types
- ✅ **Audit trail:** Automatic tracking of who created/updated credentials and when
- ✅ **Role-based access control (RBAC):** Fine-grained permissions for credential operations
- ✅ **Encrypted storage:** All credentials encrypted at rest in etcd
- ✅ **RESTful interface:** Easy integration with CI/CD pipelines and automation tools

### 7.2 Configure Docker Engine Properties

Add to your CPS properties:

```properties
# Docker engine configuration
docker.engine.PRIMARY.hostname=192.168.1.100
docker.engine.PRIMARY.port=2376
docker.engine.PRIMARY.credentials.id=DOCKER_TLS
```

**Note:** When `credentials.id` is specified, Galasa automatically uses HTTPS.

### 7.3 Example Test Code

```java
import dev.galasa.Test;
import dev.galasa.docker.DockerContainer;
import dev.galasa.docker.IDockerContainer;

@Test
public class DockerTlsTest {
    
    @DockerContainer(image = "alpine:latest", dockerEngineTag = "PRIMARY")
    public IDockerContainer container;
    
    @Test
    public void testDockerConnection() throws Exception {
        // Container will be provisioned using TLS connection
        String output = container.exec("echo", "Hello from secure Docker!");
        System.out.println(output);
    }
}
```

---

## Step 8: Test the Connection

### 8.1 Verify Galasa Configuration

```bash
# Test with curl using the same certificates
curl --cacert ca.pem \
     --cert cert.pem \
     --key key.pem \
     https://192.168.1.100:2376/version
```

### 8.2 Run a Simple Galasa Test

Create a minimal test to verify the connection:

```java
@Test
public void verifyDockerConnection() throws Exception {
    // This will fail if TLS is not configured correctly
    assertThat(container).isNotNull();
    assertThat(container.getDockerEngineHost()).isEqualTo("192.168.1.100");
}
```

### 8.3 Check Galasa Logs

Look for successful TLS handshake messages in the Galasa logs:

```
INFO: Configuring HTTPS for Docker engine PRIMARY
INFO: Successfully loaded KeyStore credentials: DOCKER_TLS
INFO: Docker engine PRIMARY connected via HTTPS
```

---

## Common Issues and Solutions

### Issue 1: "Certificate does not match the root of the given chain"

**Cause:** The CA certificate is not properly included in the KeyStore as a trusted certificate.

**Solution:**
```bash
# Verify CA is in KeyStore
keytool -list -keystore docker-tls.p12 -storepass changeit | grep docker-ca

# If missing, re-import CA certificate
keytool -importcert -alias docker-ca -file ca.pem \
  -keystore docker-tls.p12 -storepass changeit -noprompt
```

### Issue 2: "unable to find valid certification path to requested target"

**Cause:** The KeyStore doesn't contain the CA certificate needed to validate the server certificate.

**Solution:**
1. Verify the CA certificate is in the KeyStore (see Issue 1)
2. Ensure the server certificate was signed by the same CA
3. Check that the CA certificate is marked as a trusted certificate entry

### Issue 3: "Connection refused" to Docker daemon

**Cause:** Docker daemon is not listening on the TLS port, or firewall is blocking the connection.

**Solution:**
```bash
# Check if Docker is listening on port 2376
sudo netstat -tlnp | grep 2376

# Check Docker daemon configuration
sudo cat /etc/docker/daemon.json

# Check firewall rules
sudo iptables -L -n | grep 2376

# Restart Docker daemon
sudo systemctl restart docker
```

### Issue 4: "KeyStore data must be base64 encoded with 'base64:' prefix"

**Cause:** The KeyStore data in Galasa configuration is missing the required "base64:" prefix.

**Solution:**

For file-based configuration:
```properties
# Wrong:
secure.credentials.DOCKER_TLS.keystore=MIIKEQIBAzCCCdcGCSqGSIb3DQE...

# Correct:
secure.credentials.DOCKER_TLS.keystore=base64:MIIKEQIBAzCCCdcGCSqGSIb3DQE...
```

For Secrets REST API:
```json
{
  "data": {
    "keystore": "base64:MIIKEQIBAzCCCdcGCSqGSIb3DQE...",
    "password": "changeit",
    "type": "PKCS12"
  },
  "encoding": "base64"
}
```
**Note:** The "base64:" prefix is required in both file-based and API configurations.

### Issue 5: "Unsupported KeyStore type"

**Cause:** Galasa only supports PKCS12 and JKS KeyStore formats.

**Solution:**
```bash
# Convert to PKCS12 if using another format
keytool -importkeystore \
  -srckeystore old-keystore.jks \
  -srcstoretype JKS \
  -destkeystore docker-tls.p12 \
  -deststoretype PKCS12
```

### Issue 6: "Hostname verification failed"

**Cause:** The hostname in the Docker engine configuration doesn't match the Subject Alternative Names (SAN) in the server certificate.

**Solution:**
1. Verify the SAN in the server certificate:
   ```bash
   openssl x509 -in server-cert.pem -text -noout | grep -A 1 "Subject Alternative Name"
   ```
2. Ensure your `docker.engine.PRIMARY.hostname` matches one of the SANs
3. If needed, regenerate the server certificate with correct SANs

### Issue 7: Docker daemon fails to start after TLS configuration

**Cause:** Invalid certificate paths or permissions in `daemon.json`.

**Solution:**
```bash
# Check Docker logs
sudo journalctl -u docker -n 50

# Verify certificate files exist and have correct permissions
ls -la /etc/docker/certs/

# Verify daemon.json syntax
cat /etc/docker/daemon.json | python -m json.tool

# Test Docker daemon configuration
sudo dockerd --validate
```

### Issue 8: "Failed to load KeyStore" or "Password incorrect"

**Cause:** The password in Galasa configuration doesn't match the KeyStore password, or the KeyStore data is corrupted.

**Solution:**
```bash
# Test KeyStore password locally
keytool -list -keystore docker-tls.p12 -storepass YOUR_PASSWORD

# If using Secrets API, the validation happens immediately:
# - POST/PUT requests will return 400 Bad Request with error details
# - Check the error message for specific issues

# Update Galasa configuration with correct password
secure.credentials.DOCKER_TLS.password=YOUR_PASSWORD
```

---

## Security Best Practices

### 1. Protect Private Keys

```bash
# Set restrictive permissions
chmod 0400 ca-key.pem server-key.pem key.pem

# Store CA key offline after certificate generation
mv ca-key.pem /secure/offline/storage/
```

### 2. Use Strong Passwords

```bash
# Generate a strong random password
openssl rand -base64 32
```

### 3. Certificate Rotation

```bash
# Set certificate expiry reminders
# Regenerate certificates before expiry (typically 1 year)
openssl x509 -in cert.pem -noout -enddate
```

### 4. Limit Certificate Scope

- Use separate CAs for different environments (dev, test, prod)
- Create unique client certificates for each Galasa instance
- Revoke compromised certificates immediately

### 5. Network Security

```bash
# Restrict Docker TLS port to specific IPs
sudo iptables -A INPUT -p tcp --dport 2376 -s 192.168.1.0/24 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 2376 -j DROP
```

---

## Additional Resources

### Docker TLS Documentation
- [Protect the Docker daemon socket](https://docs.docker.com/engine/security/protect-access/)
- [Docker TLS configuration](https://docs.docker.com/engine/security/https/)

### OpenSSL Documentation
- [OpenSSL Certificate Authority](https://www.openssl.org/docs/man1.1.1/man1/ca.html)
- [OpenSSL x509 Certificate](https://www.openssl.org/docs/man1.1.1/man1/x509.html)

### Java KeyStore Documentation
- [keytool Reference](https://docs.oracle.com/en/java/javase/17/docs/specs/man/keytool.html)
- [PKCS12 KeyStore](https://docs.oracle.com/en/java/javase/17/security/pkcs12-keystore-enhancements.html)

---

## Summary

You have successfully:

1. ✅ Created a Certificate Authority for signing certificates
2. ✅ Generated server certificates for Docker daemon
3. ✅ Generated client certificates for Galasa
4. ✅ Configured Docker daemon to use TLS
5. ✅ Created a PKCS12 KeyStore containing certificates
6. ✅ Base64 encoded the KeyStore for Galasa
7. ✅ Configured Galasa to use the certificates (via file or REST API)
8. ✅ Tested the secure connection

Your Docker daemon is now protected with TLS, and Galasa can securely connect using client certificate authentication.

### Next Steps

- **For production use:** Consider implementing certificate rotation policies
- **For multiple engines:** Repeat the process for each Docker engine with unique certificates
- **For automation:** Use the Secrets REST API to automate credential management in CI/CD pipelines
- **For security audits:** Review the audit trail in Galasa's credential metadata
