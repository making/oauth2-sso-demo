
```
pushd src/main/resources
openssl genrsa -out private_key.pem 4096
openssl rsa -in private_key.pem -pubout -out public.pem
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