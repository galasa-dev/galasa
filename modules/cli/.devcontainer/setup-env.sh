
# Go get the public IBM certificates and install them 
curl -k -o carootcert.der https://ibmca-prod.dal.cpc.ibm.com:9443/cybersecurity/ibmcert/web/downloadCarootCert.do?file=carootcert.der
curl -k -o caintermediatecert.der https://ibmca-prod.dal.cpc.ibm.com:9443/cybersecurity/ibmcert/web/downloadCarootCert.do?file=caintermediatecert.der

cp carootcert.der $JAVA_HOME/lib/security
cp caintermediatecert.der $JAVA_HOME/lib/security

cd $JAVA_HOME/lib/security;
keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias ibmca -file carootcert.der
keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias ibminter -file caintermediatecert.der

