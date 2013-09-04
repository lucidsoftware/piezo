
SHELL := /bin/bash -e

.PHONY: default
default: all

.PHONY: package
package:
	sbt-0.12.2 package

.PHONY: test
test:
	sbt-0.12.2 test

.PHONY: all
all: compile test

.PHONY: publish
publish:
	sbt-0.12.2 publish

.PHONY: fullbuild
fullbuild:
	sbt-0.12.2 clean compile "test" package publish

.PHONY: %
%:
	sbt-0.12.2 $*
