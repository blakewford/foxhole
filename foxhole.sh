#!/bin/bash
if [ $# = 3 ]; then
	echo "Copy and expand webapp..."
	cp -fr $3/$1/ $1
	cd $1
if [ -e application.zip ]; then
	unzip -o application.zip -d assets
	rm application.zip manifest.webapp
fi
	echo "Create new Android project..."
	$2/tools/android create project --target android-10 --name Foxhole --path . --activity FoxholeActivity --package $1
	echo "Add Foxhole source..."
	echo package $1\; > temp.java
	echo "\""$1"\"" > temp.xml
	echo "        "${1//.*/}MozActivity > temp.strings
	cat temp.java ../droid/FoxholeActivity.java > combined_activity.java
	cat temp.java ../droid/WebAppManifest.java > combined_support.java
	cat ../droid/AndroidManifest.part1 temp.xml ../droid/AndroidManifest.part2 > AndroidManifest.xml
	cat ../droid/strings.part1 temp.strings ../droid/strings.part2 > combined.xml
	rm temp.java
	rm temp.xml
	rm temp.strings
	cp -f combined_activity.java ./src/${1//./\/}/FoxholeActivity.java
	cp -f combined_support.java ./src/${1//./\/}/WebAppManifest.java
	mkdir ./res/menu
	cp -f ../droid/options.xml ./res/menu/options.xml
	cp -f combined.xml ./res/values/strings.xml
	rm combined_activity.java
	rm combined_support.java
	rm combined.xml
	cp -f ../droid/gson-2.2.2.jar ./libs
	cp -f ../droid/main.xml ./res/layout/
	echo "Build APK..."
	ant debug
	echo "Finished!"
else
	echo "Invalid number of arguments! foxhole | qualified app name | android sdk | gaia |"
fi
