#!/bin/sh

for DEVICE in `adb devices | grep -v "List" | awk '{print $1}'`
  do
    echo $DEVICE

    adb -s $DEVICE uninstall com.sickboots.sickvideos.technewsset

#    adb -s $DEVICE uninstall com.sickboots.sickvideos.joerogan
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.khan
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.nerdist
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.maxkeiser
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.androiddevs
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.codeorg
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.rt
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.vice
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.neurosoup
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.jetdaisuke
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.topgear
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.bigthink
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.pewdiepie
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.reasontv
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.xda
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.theverge
#    adb -s $DEVICE uninstall com.sickboots.sickvideos.justinbieber

  done



