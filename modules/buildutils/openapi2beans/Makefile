#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
all: tests docs openapi2beans

openapi2beans: \
	bin/openapi2beans-linux-x86_64 \
	bin/openapi2beans-darwin-x86_64 \
	bin/openapi2beans-darwin-arm64 


tests: openapi2beans-source build/coverage.txt

build/coverage.out : openapi2beans-source
	mkdir -p build
	go test -v -cover -coverprofile=build/coverage.out -coverpkg ./pkg/generator,./pkg/errors,./pkg/cmd,./pkg/embedded,./pkg/utils ./pkg/...

build/coverage.html : build/coverage.out
	go tool cover -html=build/coverage.out -o build/coverage.html

build/coverage.txt : build/coverage.out
	go tool cover -func=build/coverage.out > build/coverage.txt

openapi2beans-source : \
	./cmd/openapi2beans/*.go \
	./pkg/generator/*.go \
	./pkg/cmd/*.go \
	./pkg/embedded/*.go \
	./pkg/utils/*.go \
	./pkg/errors/*.go 

bin/openapi2beans-linux-x86_64 : openapi2beans-source
	CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o bin/openapi2beans-linux-x86_64 ./cmd/openapi2beans

bin/openapi2beans-darwin-x86_64 : openapi2beans-source
	CGO_ENABLED=0 GOOS=darwin GOARCH=amd64 go build -o bin/openapi2beans-darwin-x86_64 ./cmd/openapi2beans

bin/openapi2beans-darwin-arm64 : openapi2beans-source
	CGO_ENABLED=0 GOOS=darwin GOARCH=arm64 go build -o bin/openapi2beans-darwin-arm64 ./cmd/openapi2beans

bin/openapi2beans-linux-arm64 : openapi2beans-source
	CGO_ENABLED=0 GOOS=linux GOARCH=arm64 go build -o bin/openapi2beans-linux-arm64 ./cmd/openapi2beans

docs: uml-diagrams

uml-diagrams: uml/schema-types.png uml/java-class.png uml/code-structure.png

uml/%.png: uml/%.plantuml
	java -jar plantuml.jar -verbose $? 

clean:
	rm -fr bin/openapi2beans*
	rm -fr build/*
	rm -fr build/coverage*


