#!/usr/bin/env bash
DOXYGEN_BIN=$(which doxygen)
if [ -z "$DOXYGEN_BIN" ]; then
    "Please install doxygen on your system before running this script! e.g. brew install doxygen"
    exit 1
fi
doxygen doxygenConfig.cfg
CURDIR=$(pwd)
cd ../../../build/testDocs/html/
DOCDIR=$(pwd)
cd $CURDIR
echo "******"
echo "Documents generated can be found under $DOCDIR/annotated.html"
echo "******"
echo "Main test classes are listed under com.kamcord.app.application"
echo "******"