# deploy on spirit or spiritdev

DEV_HOST=	ivlbrau3
PROD_HOST=	ivlbrau4
HOSTNAME=	$(shell hostname -s)

ifeq ($(HOSTNAME),$(PROD_HOST))
	WAR_FILE=root.war
else
	WAR_FILE=./target/scala-2.10/spirit-news_2.10-1.1.war
endif

JETTY_DIR=	/usr/share/jetty/
SBT_CMD=	java -jar sbt-launch-0.12.4.jar

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
	git submodule deinit .
	git submodule init
	git submodule update
	git submodule foreach git checkout master
	git submodule foreach git pull
	@echo "==> Running SBT to package the code for deployment"
	$(SBT_CMD) clean update	
	$(SBT_CMD) compile package
endif
ifeq ($(HOSTNAME),$(PROD_HOST))
	wget -N http://spiritdev.fh-schmalkalden.de/news/news.war
	mv news.war root.war 
endif
	@echo "==> $(WAR_FILE) prepared, now type 'make install'"

install:
ifeq ($(HOSTNAME),$(PROD_HOST))
	@echo "==> Do you really want to deploy on the live system?"
	@echo "==> Press enter or Ctrl-C to abort"
	@read input
endif
	@echo "==> Copying war file to /usr/share/jetty/webapps/"
ifeq ($(HOSTNAME),$(PROD_HOST))
	cp $(WAR_FILE) $(JETTY_DIR)/webapps/root.war
endif
ifeq ($(HOSTNAME),$(DEV_HOST))
	cp $(WAR_FILE) $(JETTY_DIR)/webapps/news.war
endif
ifeq ($(HOSTNAME),$(DEV_HOST))
	@echo "==> Copying settings to jetty folder"
	cp settings.properties $(JETTY_DIR)
	cp changeable.properties $(JETTY_DIR)
endif
ifeq ($(HOSTNAME),$(DEV_HOST))
	@echo "==> Backing up schedule"
	tar -cf $(JETTY_DIR)/tmp/schedule.tar $(JETTY_DIR)/webapps/news/staticschedule
	@echo "==> Deleting and creating news dir in webapps"
	rm -r $(JETTY_DIR)/webapps/news
	mkdir $(JETTY_DIR)/webapps/news
	@echo "==> Copying news.war into webapps/news folder and extracting it"
	cp $(JETTY_DIR)/webapps/news.war /usr/share/jetty/webapps/news
	cd $(JETTY_DIR)/webapps/news/ && jar xf news.war
	@echo "==> Setting up folder and permissions at staticschedule/plan for scheduler"
	cd / && tar -xvf $(JETTY_DIR)/tmp/schedule.tar
	@echo "==> type 'make jetty-restart'"
endif
ifeq ($(HOSTNAME),$(PROD_HOST))
	@echo "==> Backing up schedule"
	tar -cf $(JETTY_DIR)/tmp/schedule.tar $(JETTY_DIR)/webapps/root/staticschedule
	@echo "==> Deleting and creating root dir in webapps"
	rm -r $(JETTY_DIR)/webapps/root
	mkdir $(JETTY_DIR)/webapps/root
	@echo "==> Copying root.war into webapps/root folder and extracting it"
	cp $(JETTY_DIR)/webapps/root.war /usr/share/jetty/webapps/root
	cd $(JETTY_DIR)/webapps/root/ && jar xf root.war
	@echo "==> Setting up folder and permissions at staticschedule/plan for scheduler"
	cd / && tar -xvf $(JETTY_DIR)/tmp/schedule.tar
	@echo "==> type 'make jetty-restart'"
endif

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
