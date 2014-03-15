
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

.PHONY: send-key
send-key:
	sbt "pgp-cmd send-key support@lucidchart.com hkp://pgp.mit.edu"

.PHONY: publish-signed
publish-signed:
	@echo GPG_AGENT_INFO: $(value GPG_AGENT_INFO)
	@echo SONATYPE_USERNAME: $(value SONATYPE_USERNAME)
	sbt publish-signed

.PHONY: cleanpackage
cleanpackage:
	sbt clean compile "test" package

.PHONY: %
%:
	sbt $*
