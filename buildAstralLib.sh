#!/bin/bash

mkdir -p ./libs/

current=$(pwd)

cd ../astrald || exit 1

gomobile bind -v -o ../astral-android/libs/astral-message.aar -target=android ./bind/msg ./bind/binary

cd "$current" || exit 2
