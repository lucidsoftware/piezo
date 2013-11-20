
SHELL := /bin/bash -e

.PHONY: default
default: all

.PHONY: package
package:
	sbt package

.PHONY: test
test:
	sbt test

.PHONY: all
all: compile test

.PHONY: stage
stage:
	sbt stage

.PHONY: publish
publish:
	sbt publish

.PHONY: publish-signed
publish-signed:
	sbt publish-signed

.PHONY: fullbuild
fullbuild:
	sbt clean compile "test" package publish

.PHONY: %
%:
	sbt $*
