#
# Makefile for Timeless
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

deploy:
	@ git push heroku master

start:
	@ heroku ps:scale web=1

stop:
	@ heroku ps:scale web=0

smoke/production:
	@ $(SMOKE) prod all

clean:
	@ $(LEIN) clean
	@ rm -rf $(TARGET_DIR)

tests:
	@ $(LEIN) do clean, test

run-uberjar: jar
	@ echo 'Running the uberjar...'
	@ java -Xmx512m -cp $(TARGET_DIR)/timeless-standalone.jar timeless.main web
