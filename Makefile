#
# Makefile for Newsroom
#
ifeq ($(shell uname),Linux)
MAKE = make
LEIN = lein
endif
ifeq ($(shell uname),Darwin)
MAKE = make
LEIN = lein
endif

#
# These are the locations of the directories we'll use
#
SRC_DIR = src
TEST_DIR = test
TARGET_DIR = target
#
# These are a few of the scripts/commands that are WITHIN the project
#
DEPLOY = bin/deploy
SMOKE = bin/smoke
HIPCHAT = deploy/bin/hipchat
VERCHG = bin/verchg
VER = $(shell head -1 project.clj | sed -e 's/^.* \"//' -e 's/\"//')
SHA ?= $(shell git rev-parse --abbrev-ref HEAD)
PSHA = $(shell git rev-parse $(SHA) | head -c 8)
VERSHA = $(VER)-$(PSHA)
#
# These are the machines we'll be deploying to
#
GRA_APP = newsroom-jv01.grarate.com \
		   newsroom-jv02.grarate.com \
		   newsroom-jv03.grarate.com \
		   newsroom-jv04.grarate.com
PROD_APP = newsroom-prod01.guaranteedrate.com \
		   newsroom-prod02.guaranteedrate.com \
		   newsroom-prod03.guaranteedrate.com \
		   newsroom-prod04.guaranteedrate.com \
		   newsroom-prod05.guaranteedrate.com \
		   newsroom-prod06.guaranteedrate.com
DEV_APP = newsroom-dev01.gr-dev.com
INIT_D = newsroom

#
# These are the main targets that we'll be making
#
all: jar tests

version/major:
	@ echo 'Updating major version and adding CHANGELOG entry...'
	@ $(VERCHG) 'major'

version/minor:
	@ echo 'Updating minor version and adding CHANGELOG entry...'
	@ $(VERCHG) 'minor'

version/bugfix:
	@ echo 'Updating bugfix version and adding CHANGELOG entry...'
	@ $(VERCHG) 'bugfix'

version:
	@ echo 'Adding CHANGELOG entry for existing version...'
	@ $(VERCHG) 'push'

jar:
	@ echo 'Building uberjar...'
	@ $(LEIN) uberjar &>/dev/null

deploy/production:
	@ $(HIPCHAT) -f "Newsroom" "Deploying $(VERSHA) to production..." >/dev/null
	@ $(DEPLOY) prod

start/production:
	@ $(HIPCHAT) -f "Newsroom" "Starting production..." >/dev/null
	@ for mach in $(PROD_APP); do \
	    ssh $$mach "sudo /etc/init.d/$(INIT_D) start" ; \
	  done
	@ echo ''

stop/production:
	@ $(HIPCHAT) -f "Newsroom" "Stopping production..." >/dev/null
	@ for mach in $(PROD_APP); do \
	    ssh $$mach "sudo /etc/init.d/$(INIT_D) stop" ; \
	  done
	@ echo ''

restart/production:
	@ $(MAKE) stop/production
	@ $(MAKE) start/production

roll/production:
	@ $(HIPCHAT) -f "Newsroom" "Rolling production..." >/dev/null
	@ for mach in $(PROD_APP); do \
	    ssh $$mach "sudo /etc/init.d/$(INIT_D) restart" ; \
	  done
	@ echo ''

smoke/production:
	@ $(HIPCHAT) -f "Newsroom" "Smoke testing production..." >/dev/null
	@ $(SMOKE) prod all

deploy/affinity:
	@ $(HIPCHAT) -f "Newsroom" "Deploying $(VERSHA) to affinity..." >/dev/null
	@ $(DEPLOY) affinity

start/affinity:
	@ $(HIPCHAT) -f "Newsroom" "Starting affinity..." >/dev/null
	@ for mach in $(GRA_APP); do \
	    ssh $$mach "sudo /etc/init.d/$(INIT_D) start" ; \
	  done
	@ echo ''

stop/affinity:
	@ $(HIPCHAT) -f "Newsroom" "Stopping affinity..." >/dev/null
	@ for mach in $(GRA_APP); do \
	    ssh $$mach "sudo /etc/init.d/$(INIT_D) stop" ; \
	  done
	@ echo ''

restart/affinity:
	@ $(MAKE) stop/affinity
	@ $(MAKE) start/affinity

roll/affinity:
	@ $(HIPCHAT) -f "Newsroom" "Rolling affinity..." >/dev/null
	@ for mach in $(GRA_APP); do \
	    ssh $$mach "sudo /etc/init.d/$(INIT_D) restart" ; \
	  done
	@ echo ''

smoke/affinity:
	@ $(HIPCHAT) -f "Newsroom" "Smoke testing affinity..." >/dev/null
	@ $(SMOKE) affinity all

deploy/dev:
	@ $(HIPCHAT) -f "Newsroom" "Deploying $(VERSHA) to dev..." >/dev/null
	@ $(DEPLOY) dev

start/dev:
	@ $(HIPCHAT) -f "Newsroom" "Starting dev..." >/dev/null
	@ for mach in $(DEV_APP); do \
	    ssh $$mach "sudo /etc/init.d/$(INIT_D) start" ; \
	  done
	@ echo ''

stop/dev:
	@ $(HIPCHAT) -f "Newsroom" "Stopping dev..." >/dev/null
	@ for mach in $(DEV_APP); do \
	    ssh $$mach "sudo /etc/init.d/$(INIT_D) stop" ; \
	  done
	@ echo ''

restart/dev:
	@ $(MAKE) stop/dev
	@ $(MAKE) start/dev

roll/dev:
	@ $(HIPCHAT) -f "Newsroom" "Rolling dev..." >/dev/null
	@ for mach in $(DEV_APP); do \
	    ssh $$mach "sudo /etc/init.d/$(INIT_D) restart" ; \
	  done
	@ echo ''

smoke/dev:
	@ $(HIPCHAT) -f "Newsroom" "Smoke testing dev..." >/dev/null
	@ $(SMOKE) dev all

clean:
	@ $(LEIN) clean
	@ rm -rf $(TARGET_DIR)

tests:
	@ $(LEIN) do clean, test

run-uberjar: jar
	@ echo 'Running the uberjar...'
	@ java -Xmx512m -cp $(TARGET_DIR)/newsroom-$(VER)-standalone.jar newsroom.main web

keystore:
	@ keytool -importkeystore -srckeystore ~/Desktop/grdev\ wildcard\ cert\ 3yr.pfx \
			  -srcstoretype pkcs12 -srcstorepass Guaranteed1 \
			  -destkeystore resources/certs/key_crt.jks -deststoretype jks \
			  -deststorepass Guaranteed1
