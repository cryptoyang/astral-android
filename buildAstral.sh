#!/bin/bash

mkdir -p ./libs/

current=$(pwd)

cd ../astrald || exit 1

gomobile bind -v -o ../astral-android/libs/astral.aar -target=android ./mobile/ ./java/

cd "$current" || exit 2
