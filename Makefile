
.PHONY: admin install source-deb

SBT = sbt
SBT_DEPS =
SBT_VERSION = 1.5.0

ifeq "$(shell which $(SBT) 2>/dev/null)" ""
	SBT_DEPS = sbt/bin/sbt
	SBT = sbt/bin/sbt
endif

admin-stage: $(SBT_DEPS)
	$(SBT) 'admin / stage'

STAGE=admin/target/universal/stage

INSTALL_DEPS:=""

install:
	install -D -t "$(DESTDIR)/usr/share/piezo-admin/lib" "$(STAGE)"/lib/*
	install -D -t "$(DESTDIR)/usr/share/piezo-admin/conf" "$(STAGE)"/conf/*
	install -D -m755 $(STAGE)/bin/piezo-admin "$(DESTDIR)/usr/share/piezo-admin/bin/piezo-admin"
	install -D admin/src/linux/piezo-admin.service "$(DESTDIR)/lib/systemd/system/piezo-admin.service"
	install -d "$(DESTDIR)/usr/bin" "$(DESTDIR)/etc/"
	ln -sf /usr/share/piezo-admin/bin/piezo-admin "$(DESTDIR)/usr/bin/piezo-admin"
	ln -sfn /usr/share/piezo-admin/conf "$(DESTDIR)/etc/piezo-admin"
	ln -sfn /var/log/piezo-admin "$(DESTDIR)/usr/share/piezo-admin/logs"

clean:
	rm -rf */target/ target/ sbt/ sbt.tgz project/project/


source-deb:
	debuild --no-tgz-check -S --diff-ignore --tar-ignore

sbt/bin/sbt: sbt.tgz
	tar xzf sbt.tgz

sbt.tgz:
	wget -O sbt.tgz https://github.com/sbt/sbt/releases/download/v$(SBT_VERSION)/sbt-$(SBT_VERSION).tgz
