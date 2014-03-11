#!/bin/sh

cd ../SickVideos/build/apk

for DEVICE in `adb devices | grep -v "List" | awk '{print $1}'`
  do
    echo $DEVICE

    adb -s $DEVICE install SickVideos-technews-release.apk
    adb -s $DEVICE install SickVideos-youtube-release.apk
    adb -s $DEVICE install SickVideos-svb-release.apk

#    adb -s $DEVICE install SickVideos-nerdist-release.apk
#    adb -s $DEVICE install SickVideos-neurosoup-release.apk
#    adb -s $DEVICE install SickVideos-maxkeiser-release.apk
#    adb -s $DEVICE install SickVideos-androiddevs-release.apk
#    adb -s $DEVICE install SickVideos-joerogan-release.apk
#    adb -s $DEVICE install SickVideos-jetdaisuke-release.apk
#    adb -s $DEVICE install SickVideos-bigthink-release.apk
#    adb -s $DEVICE install SickVideos-theverge-release.apk
#    adb -s $DEVICE install SickVideos-reasontv-release.apk
#    adb -s $DEVICE install SickVideos-codeorg-release.apk
#    adb -s $DEVICE install SickVideos-rt-release.apk
#    adb -s $DEVICE install SickVideos-vice-release.apk
#    adb -s $DEVICE install SickVideos-khan-release.apk
#    adb -s $DEVICE install SickVideos-topgear-release.apk
#    adb -s $DEVICE install SickVideos-pewdiepie-release.apk
#    adb -s $DEVICE install SickVideos-xda-release.apk
#    adb -s $DEVICE install SickVideos-justinbieber-release.apk

  done





