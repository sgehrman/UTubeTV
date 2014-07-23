#!/bin/sh

cd ../UTubeTV/build/outputs/apk

for DEVICE in `adb devices | grep -v "List" | awk '{print $1}'`
  do
    echo $DEVICE

    adb -s $DEVICE install UTubeTV-technews-release.apk

    adb -s $DEVICE install UTubeTV-youtube-release.apk
    adb -s $DEVICE install UTubeTV-svb-release.apk
    adb -s $DEVICE install UTubeTV-nerdist-release.apk
    adb -s $DEVICE install UTubeTV-neurosoup-release.apk
    adb -s $DEVICE install UTubeTV-maxkeiser-release.apk
    adb -s $DEVICE install UTubeTV-androiddevs-release.apk
    adb -s $DEVICE install UTubeTV-joerogan-release.apk
    adb -s $DEVICE install UTubeTV-jetdaisuke-release.apk
    adb -s $DEVICE install UTubeTV-bigthink-release.apk
    adb -s $DEVICE install UTubeTV-theverge-release.apk
    adb -s $DEVICE install UTubeTV-reasontv-release.apk
    adb -s $DEVICE install UTubeTV-codeorg-release.apk
    adb -s $DEVICE install UTubeTV-rt-release.apk
    adb -s $DEVICE install UTubeTV-vice-release.apk
    adb -s $DEVICE install UTubeTV-khan-release.apk
    adb -s $DEVICE install UTubeTV-topgear-release.apk
    adb -s $DEVICE install UTubeTV-pewdiepie-release.apk
    adb -s $DEVICE install UTubeTV-xda-release.apk
    adb -s $DEVICE install UTubeTV-justinbieber-release.apk
    adb -s $DEVICE install UTubeTV-lukitsch-release.apk

  done





