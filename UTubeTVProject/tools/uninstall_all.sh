#!/bin/sh

for DEVICE in `adb devices | grep -v "List" | awk '{print $1}'`
  do
    echo $DEVICE

    adb -s $DEVICE uninstall re.distantfutu.videos.technews
    adb -s $DEVICE uninstall re.distantfutu.videos.youtube
    adb -s $DEVICE uninstall re.distantfutu.videos.svb

    adb -s $DEVICE uninstall re.distantfutu.videos.nerdist
    adb -s $DEVICE uninstall re.distantfutu.videos.neurosoup
    adb -s $DEVICE uninstall re.distantfutu.videos.maxkeiser
    adb -s $DEVICE uninstall re.distantfutu.videos.androiddevs
    adb -s $DEVICE uninstall re.distantfutu.videos.joerogan
    adb -s $DEVICE uninstall re.distantfutu.videos.jetdaisuke
    adb -s $DEVICE uninstall re.distantfutu.videos.bigthink
    adb -s $DEVICE uninstall re.distantfutu.videos.theverge
    adb -s $DEVICE uninstall re.distantfutu.videos.reasontv
    adb -s $DEVICE uninstall re.distantfutu.videos.khan
    adb -s $DEVICE uninstall re.distantfutu.videos.codeorg
    adb -s $DEVICE uninstall re.distantfutu.videos.rt
    adb -s $DEVICE uninstall re.distantfutu.videos.vice
    adb -s $DEVICE uninstall re.distantfutu.videos.topgear
    adb -s $DEVICE uninstall re.distantfutu.videos.pewdiepie
    adb -s $DEVICE uninstall re.distantfutu.videos.xda
    adb -s $DEVICE uninstall re.distantfutu.videos.justinbieber
    adb -s $DEVICE uninstall re.distantfutu.videos.lukitsch

  done



