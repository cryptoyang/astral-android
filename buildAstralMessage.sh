#!/bin/bash

mkdir -p ./libs/

current=$(pwd)

cd ../astrald || exit 1

gomobile bind -v -o ../astral-android/libs/astral-msg.aar -target=android ./bind/msg

cd "$current" || exit 2
