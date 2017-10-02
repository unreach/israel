## SSL

```

openssl genrsa -out private.pem 1024  // 2048会更安全点，但是效率低
// 这里将生成一个新的文件server.pem，即一个证书请求文件，你可以拿着这个文件去数字证书颁发机构（即CA）申请一个数字证书。CA会给你一个新的文件ca.pem，那才是你的数字证书。
openssl req -new  -x509 -key private.pem -out server.pem
// 100年
openssl req -new -x509 -key private.pem -out ca.pem -days 36500

//for netty ,生成netty需要的私有证书
openssl pkcs8 -topk8 -inform PEM -in private.pem -outform PEM -nocrypt -out server_pkcs8.key
// 查看证书
openssl x509 -in ca.pem -inform pem -noout -text


```

