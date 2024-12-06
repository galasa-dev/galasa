all: clean test \
	bin/galasabld-linux-amd64 \
	bin/galasabld-windows-amd64 \
	bin/galasabld-darwin-amd64 \
	bin/galasabld-linux-s390x \
	bin/galasabld-darwin-arm64 \
	bin/galasabld-linux-arm64

src : ./Makefile \
	./cmd/galasabld/main.go \
	./pkg/cmd/*.go \
	./pkg/galasayaml/*.go \
	./pkg/githubjson/*.go \
	./pkg/utils/*.go \
	./pkg/versioning/*.go

test: src

bin/galasabld-linux-amd64 : src
	CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o bin/galasabld-linux-amd64 ./cmd/galasabld

bin/galasabld-linux-arm64 : src
	CGO_ENABLED=0 GOOS=linux GOARCH=arm64 go build -o bin/galasabld-linux-arm64 ./cmd/galasabld

bin/galasabld-windows-amd64 : src
	CGO_ENABLED=0 GOOS=windows GOARCH=amd64 go build -o bin/galasabld-windows-amd64 ./cmd/galasabld

bin/galasabld-darwin-amd64 : src
	CGO_ENABLED=0 GOOS=darwin GOARCH=amd64 go build -o bin/galasabld-darwin-amd64 ./cmd/galasabld

bin/galasabld-darwin-arm64 : src
	CGO_ENABLED=0 GOOS=darwin GOARCH=arm64 go build -o bin/galasabld-darwin-arm64 ./cmd/galasabld

bin/galasabld-linux-s390x : src
	CGO_ENABLED=0 GOOS=linux GOARCH=s390x go build -o bin/galasabld-linux-s390x ./cmd/galasabld

test: src build/coverage.txt build/coverage.html build/coverage.out

build/coverage.out : src
	mkdir -p build
	go test -v -cover -coverprofile=build/coverage.out -coverpkg ./pkg/cmd,./pkg/galasayaml,./pkg/githubjson,./pkg/utils,./pkg/versioning ./pkg/...

build/coverage.html : build/coverage.out
	go tool cover -html=build/coverage.out -o build/coverage.html

build/coverage.txt : build/coverage.out
	go tool cover -func=build/coverage.out > build/coverage.txt
	cat build/coverage.txt

clean:
	rm -rf bin
	rm -rf build/coverage.*
