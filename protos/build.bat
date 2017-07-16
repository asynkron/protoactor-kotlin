protoc *.proto -I. -I.. --java_out=.  
protoc *.proto -I. -I.. --grpc-java_out=. --plugin=protoc-gen-grpc-java=c:\proto\bin\protoc-gen-grpc-java.exe
