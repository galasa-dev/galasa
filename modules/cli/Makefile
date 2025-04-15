#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

# Rather than keep tabs on all of the source folders, lets create the list of things we are dependent upon 
# dynamically using a cool shell script.

# A list of source files. This is completely expanded-out.
# Evaluates to something like this: ./pkg/tokensformatter/tokensFormatter.go ./pkg/tokensformatter/summaryFormatter_test.go
# We want to run some targets if any of the source files have changed.
SOURCE_FILES := $(shell find . -iname "*.go"| tr "\n" " ") embedded_info

all: tests galasactl gendocs-galasactl

galasactl: \
	bin/galasactl-linux-arm64 \
	bin/galasactl-linux-x86_64 \
	bin/galasactl-windows-x86_64.exe \
	bin/galasactl-darwin-x86_64 \
	bin/galasactl-darwin-arm64 \
	bin/galasactl-linux-s390x

# 'gendocs-galasactl' is a command-line tool which generates documentation about the galasactl tool.
# When executed, the .md produced contain up-to-date information on tool syntax.
gendocs-galasactl: \
	bin/gendocs-galasactl-darwin-arm64 \
	bin/gendocs-galasactl-linux-arm64 \
	bin/gendocs-galasactl-darwin-x86_64 \
	bin/gendocs-galasactl-linux-x86_64

tests: $(SOURCE_FILES) build/coverage.txt build/coverage.html

# Build a list of the source packages
# Note:
# - We don't want to include the generated galasaapi
# - We don't want to include the top-level command code at ./cmd
# - We join them into a comma-separated list
# - We smarten-up the begining element (remove ".,")
# - We smarten-up the end element (remove trailing ",")
# - We remove any spaces in the whole thing,.
# So we end up with something like this: ./pkg/api,./pkg/auth,./pkg/cmd,./pkg/embedded,./pkg/errors,./pkg/files ...etc.
COVERAGE_SOURCE_PACKAGES := ${shell find . -iname "*.go" \
	| xargs dirname {} \
	| grep -v "/pkg/galasaapi" \
	| grep -v "[.]/cmd/" \
	| sort \
	| uniq \
	| tr '\n' ',' \
	| sed "s/[.],//g" \
	| sed "s/,$$//g" \
	| sed "s/ //g" \
}

# If any source code has changed, re-run the unit tests.
build/coverage.out : $(SOURCE_FILES)
	mkdir -p build
	echo "Coverage source packages are $(COVERAGE_SOURCE_PACKAGES)"
	go test -v -cover -coverprofile=build/coverage.out -coverpkg $(COVERAGE_SOURCE_PACKAGES) ./pkg/...

build/coverage-sanitised.out : build/coverage.out
	cat build/coverage.out \
		| grep -v "Mock" \
		| grep -v "ixture" \
		> build/coverage-sanitised.out

# Unit test output --> an html report.
build/coverage.html : build/coverage-sanitised.out
	go tool cover -html=build/coverage.out -o build/coverage.html

# Unit test output --> a text file report.
build/coverage.txt : build/coverage-sanitised.out
	go tool cover -func=build/coverage.out > build/coverage.txt
	cat build/coverage.txt

coverage : build/coverage.txt

# The build process
GENERATED_BUILD_PROPERTIES_FILE := pkg/embedded/templates/version/build.properties
embedded_info : $(GENERATED_BUILD_PROPERTIES_FILE)
	

pkg/embedded/templates/version :
	mkdir -p $@

# Build a properties file containing versions of things.
# Then the galasactl can embed the data and read it at run-time.
$(GENERATED_BUILD_PROPERTIES_FILE) : VERSION pkg/embedded/templates/version Makefile build.gradle
	echo "# Property file generated at build-time" > $@
	# Turn the contents of VERSION file into a properties file value.
	cat VERSION | sed "s/^/galasactl.version = /1" >> $@ ; echo "" >> $@
	# Add the `galasa.boot.jar.version` property based on the build.gradle value.
	cat build.gradle | grep "def galasaVersion" | cut -f2 -d\' | sed "s/^/galasa.boot.jar.version = /" >> $@
	# Add the `galasa.framework.version` property based on the build.gradle value.
	cat build.gradle | grep "def galasaVersion" | cut -f2 -d\' | sed "s/^/galasa.framework.version = /" >> $@
	# Add the `galasactl.rest.api.version` property based on the build/dependencies/openapi.yaml value.
	echo "" >> $@
	echo "# version of the rest api that is compiled and the client is expecting from the ecosystem." >> $@
	cat build/dependencies/openapi.yaml | grep "version :" | cut -f2 -d'"' | sed "s/^/galasactl.rest.api.version = /" >> $@

bin/galasactl-linux-x86_64 : $(SOURCE_FILES)
	CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o bin/galasactl-linux-x86_64 ./cmd/galasactl

bin/galasactl-windows-x86_64.exe : $(SOURCE_FILES)
	CGO_ENABLED=0 GOOS=windows GOARCH=amd64 go build -o bin/galasactl-windows-x86_64.exe ./cmd/galasactl

bin/galasactl-darwin-x86_64 : $(SOURCE_FILES)
	CGO_ENABLED=0 GOOS=darwin GOARCH=amd64 go build -o bin/galasactl-darwin-x86_64 ./cmd/galasactl

bin/galasactl-darwin-arm64 : $(SOURCE_FILES)
	CGO_ENABLED=0 GOOS=darwin GOARCH=arm64 go build -o bin/galasactl-darwin-arm64 ./cmd/galasactl

bin/galasactl-linux-arm64 : $(SOURCE_FILES)
	CGO_ENABLED=0 GOOS=linux GOARCH=arm64 go build -o bin/galasactl-linux-arm64 ./cmd/galasactl

bin/galasactl-linux-s390x : $(SOURCE_FILES)
	CGO_ENABLED=0 GOOS=linux GOARCH=s390x go build -o bin/galasactl-linux-s390x ./cmd/galasactl

bin/gendocs-galasactl-darwin-arm64 : $(SOURCE_FILES)
	CGO_ENABLED=0 GOOS=darwin GOARCH=arm64 go build -o bin/gendocs-galasactl-darwin-arm64 ./cmd/gendocs-galasactl

bin/gendocs-galasactl-linux-arm64 : $(SOURCE_FILES)
	CGO_ENABLED=0 GOOS=linux GOARCH=arm64 go build -o bin/gendocs-galasactl-linux-arm64 ./cmd/gendocs-galasactl

bin/gendocs-galasactl-linux-x86_64 : $(SOURCE_FILES)
	CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o bin/gendocs-galasactl-linux-x86_64 ./cmd/gendocs-galasactl

bin/gendocs-galasactl-darwin-x86_64 : $(SOURCE_FILES)
	CGO_ENABLED=0 GOOS=darwin GOARCH=amd64 go build -o bin/gendocs-galasactl-darwin-x86_64 ./cmd/gendocs-galasactl

clean:
	rm -fr bin/galasactl*
	rm -fr build/*
	rm -fr build/coverage*
	rm -fr pkg/embedded/templates/version

