#!/bin/sh

echo ======== building phase ========

./build_all.sh

echo ======== installing phase ========

./uninstall_all.sh

echo ======== uninstalling phase ========

./install_all.sh

echo ======== all script done ========
