#!/usr/bin/env bash
DOXYGEN_BIN="/usr/local/bin/doxygen"
if [ ! -x $DOXYGEN_BIN ]; then
    echo "Please install doxygen on your system before running this script! e.g. brew install doxygen"
    exit 1
fi
DOCDIR="app/build/testDocs/html"
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
rm -rf $DOCDIR
mkdir -p $DOCDIR
$DOXYGEN_BIN $DIR/doxygenConfig.cfg
echo "******"
echo "Documents generated can be found under $DOCDIR/annotated.html"
echo "******"fir
echo "Main test classes are listed under com.kamcord.app.application"
echo "******"