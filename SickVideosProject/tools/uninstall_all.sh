#!/bin/sh

for DEVICE in `adb devices | grep -v "List" | awk '{print $1}'`
  do
    echo $DEVICE

    adb -s $DEVICE uninstall com.distantfuture.videos.technews
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.nerdist
#    adb -s $DEVICE uninstall com.distantfuture.videos.neurosoup
#    adb -s $DEVICE uninstall com.distantfuture.videos.maxkeiser
#    adb -s $DEVICE uninstall com.distantfuture.videos.androiddevs
#    adb -s $DEVICE uninstall com.distantfuture.videos.joerogan
#    adb -s $DEVICE uninstall com.distantfuture.videos.jetdaisuke
#    adb -s $DEVICE uninstall com.distantfuture.videos.bigthink
#    adb -s $DEVICE uninstall com.distantfuture.videos.theverge
#    adb -s $DEVICE uninstall com.distantfuture.videos.reasontv

#    adb -s $DEVICE uninstall com.distantfuture.videos.khan
#    adb -s $DEVICE uninstall com.distantfuture.videos.codeorg
#    adb -s $DEVICE uninstall com.distantfuture.videos.rt
#    adb -s $DEVICE uninstall com.distantfuture.videos.vice
#    adb -s $DEVICE uninstall com.distantfuture.videos.topgear
#    adb -s $DEVICE uninstall com.distantfuture.videos.pewdiepie
#    adb -s $DEVICE uninstall com.distantfuture.videos.xda
#    adb -s $DEVICE uninstall com.distantfuture.videos.justinbieber

  done



