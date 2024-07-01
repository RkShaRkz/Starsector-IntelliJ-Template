#!/bin/bash

# Define source and target directories
srcData="src/data"
srcGraphics="graphics"
srcSounds="sounds"
srcJars="jars"
targetDeployment="DEPLOYMENT"

# Check if DEPLOYMENT folder exists, if it doesn't exist - create it
if [ ! -d "$targetDeployment" ]; then
    mkdir "$targetDeployment"
fi

# Copy src/data to DEPLOYMENT/data
targetData="$targetDeployment/data"
if [ ! -d "$targetData" ]; then
    mkdir "$targetData"
fi
cp -r "$srcData/"* "$targetData/"

# Copy graphics to DEPLOYMENT/graphics
targetGraphics="$targetDeployment/graphics"
if [ ! -d "$targetGraphics" ]; then
    mkdir "$targetGraphics"
fi
cp -r "$srcGraphics/"* "$targetGraphics/"

# Copy sounds to DEPLOYMENT/sounds
targetSounds="$targetDeployment/sounds"
if [ ! -d "$targetSounds" ]; then
    mkdir "$targetSounds"
fi
cp -r "$srcSounds/"* "$targetSounds/"

# Copy jars to DEPLOYMENT/jars
targetJars="$targetDeployment/jars"
if [ ! -d "$targetJars" ]; then
    mkdir "$targetJars"
fi
cp -r "$srcJars/"* "$targetJars/"

# Additionally copy the necessary misc files
#mod_info.json
#vayramerged.version
#VAYRA_SETTINGS.ini
#VAYRA_SETTINGS_explained.ini

cp mod_info.json "$targetDeployment/"
cp vayramerged.version "$targetDeployment/"
cp VAYRA_SETTINGS.ini "$targetDeployment/"
cp "VAYRA_SETTINGS - explained.ini" "$targetDeployment/"
