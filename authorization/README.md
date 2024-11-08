
```
pushd src/main/resources
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -outform PEM -pubout -out public.pem
openssl pkcs8 -topk8 -inform PEM -in private.pem -out private_key.pem -nocrypt
rm -f private.pem
popd
```


```
cat <<EOF | sudo tee -a /etc/hosts
127.0.0.1	host.docker.internal
EOF

ISSUER_URL=http://host.docker.internal:9000
CLIENT_ID=dex-example-app
CLIENT_SECRET=dex
docker run --rm -p 5555:5555 obitech/dex-example-app --debug --issuer ${ISSUER_URL} --listen http://0.0.0.0:5555 --client-id ${CLIENT_ID} --client-secret ${CLIENT_SECRET} --redirect-uri=http://localhost:5555/callback
```