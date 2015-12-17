#!/bin/bash

rm -r "$1"
mkdir "$1"
cp skyghost "$1"
cp *.bb "$1"
cp *.ogg "$1"
cp *.png "$1"
cp *.ttf "$1"
cp *.sh "$1"
cp README.md "$1"
cp LICENSE "$1"
zip -r "$1".zip "$1"
