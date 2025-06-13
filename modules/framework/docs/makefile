#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

# Objective of this make file is to only build/re-build the uml diagrams ONLY if the source for the diagram has changed.
# This is supposed to make sure that the png files don't get re-generated 
# every time you run the build-locally, so don't have to be checked-in to git 
# all the time when they don't need to be.
# Make does this by always comparing timestamps on source and target files.
# If the target file is older than the source file, it re-builds things.

# List all the png files we want to re-build.
# We want the png files, as these are the things we want to build.
# There should be a png file to build for every .plantuml file there is.
# So find all the plantuml files, and change the filename from 
# plantuml to png.
diagram_files := $(shell find . -name '*.plantuml' | sed "s/plantuml/png/g")

# To build everything, build all the png files.
all: $(diagram_files)

# A fancy rule to say "if you need a png file, it depends upon a similarly-named .plantuml file.
# ie: If the plantuml file changes date/time as it's updated, then (and only then) should the 
# png file be built.
# Note: $^ refers to the thing being depended upon, in this case a .plantuml file
# and $@ refers to the thing we are tryingto build, in this case a .png file
# Note Makefiles have access to all the environment variables which 
# were exported by the calling script or shell 
%.png: %.plantuml
	echo "building diagram $@ from source file $^"
	java -jar ${BASEDIR}/temp/plantuml.jar -tpng $^

