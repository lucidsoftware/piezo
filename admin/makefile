#
# The Lucid Software piezo-admin-service build and release process
#
#

SHELL := /bin/bash -e

.PHONY: default
default: all

.PHONY: all
all : clean compile version test package

.PHONY: package
package:
	sbt package playPackageAssets
	build/createpackage $(VERSION_PREFIX)

.PHONY: deploy
deploy:
	build/deploy

.PHONY: test
test:
	sbt test

.PHONY: version
version: clean-version conf/version.conf

clean-version:
	rm -f conf/version.conf

conf/version.conf:
	echo -n "timestamp=" > $@
	date +%Y%m%d%H%M%S >> $@

.PHONY: %
%:
	sbt $*
