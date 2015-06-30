#!/usr/bin/env bash
DOXYGEN_BIN=$(which doxygen)
if [ -z "$DOXYGEN_BIN" ]; then
    "Please install doxygen on your system before running this script! e.g. brew install doxygen"
    exit 1
fi
DOCDIR="app/build/testDocs/html"
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
rm -rf $DOCDIR
mkdir -p $DOCDIR
doxygen $DIR/doxygenConfig.cfg
echo "******"
echo "Documents generated can be found under $DOCDIR/annotated.html"
echo "******"
echo "Main test classes are listed under com.kamcord.app.application"
echo "******"