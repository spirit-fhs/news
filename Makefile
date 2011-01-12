# deploy on spirit or spiritdev

DEV_HOST=	ivlbrau3
PROD_HOST=	ivlbrau4
HOSTNAME=	$(shell hostname -s)

ifeq ($(HOSTNAME),$(PROD_HOST))
	WAR_FILE=root.war
else
	WAR_FILE=./target/scala_2.8.0/spirit-news_2.8.0-1.0.war
endif

JETTY_DIR=	/usr/share/jetty/
SBT_CMD=	java -jar sbt-launch-0.7.4.jar

all: build install jetty-restart

ifeq ($(HOSTNAME),$(PROD_HOST))
bootstrap: fetch-makefile
	make all

fetch-makefile:
	wget -N --no-check-certificate https://github.com/spirit-fhs/news/raw/master/Makefile
endif

build:
ifeq ($(HOSTNAME),$(DEV_HOST))
	@echo "==> Pulling code from Github"
	git pull origin master
	@echo "==> Running SBT to package the code for deployment"
	$(SBT_CMD) clean update compile prepare-webapp
	rm target/scala_2.8.0/webapp/WEB-INF/lib/activation-1.1.jar
	$(SBT_CMD) package
endif
ifeq ($(HOSTNAME),$(PROD_HOST))
	wget -N http://spiritdev.fh-schmalkalden.de/root.war
endif
	@echo "==> $(WAR_FILE) prepared, now type 'make install'"

install:
ifeq ($(HOSTNAME),$(PROD_HOST))
	@echo "==> Do you really want to deploy on the live system?"
	@echo "==> Press enter or Ctrl-C to abort"
	@read input
endif
	@echo "==> Copying war file to /usr/share/jetty/webapps/"
	cp $(WAR_FILE) $(JETTY_DIR)/webapps/root.war
ifeq ($(HOSTNAME),$(DEV_HOST))
	@echo "==> Copying settings to jetty folder"
	cp settings.properties $(JETTY_DIR)
endif
	@echo "==> Deleting and creating root dir in webapps"
	rm -r $(JETTY_DIR)/webapps/root
	mkdir $(JETTY_DIR)/webapps/root
	@echo "==> Copying root.war into webapps/root folder and extracing it"
	cp $(JETTY_DIR)/webapps/root.war /usr/share/jetty/webapps/root
	cd $(JETTY_DIR)/webapps/root/ && jar xf root.war
	@echo "==> type 'make jetty-restart'"

jetty-restart:
	@echo "==> Restarting jetty"
	/etc/init.d/jetty stop
	/etc/init.d/jetty start
ifeq ($(HOSTNAME),$(PROD_HOST))
	@echo "http://spirit.fh-schmalkalden.de/"
	@echo "new version of news deployed on spirit" | \
		 nc -u pads.fh-schmalkalden.de 9900 -q 1

endif
ifeq ($(HOSTNAME),$(DEV_HOST))
	@echo "http://spiritdev.fh-schmalkalden.de/"
	@echo "new version of news deployed on spiritdev" | \
		 nc -u pads.fh-schmalkalden.de 9900 -q 1
endif

deploydev:
	@ssh root@spiritdev.fh-schmalkalden.de "cd news && make all"
