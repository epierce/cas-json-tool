#!/bin/bash

VERSION="0.2.0"

if [ ! -n "$1" ]; then
    echo "Usage: install_tool.sh <install_dir>"
    exit 1
fi

if [ ! -d "$1/cas-json-tool-$VERSION" ] ; then
    echo "Creating directory structure"
    mkdir $1/cas-json-tool-$VERSION
    mkdir $1/cas-json-tool-$VERSION/bin
    mkdir $1/cas-json-tool-$VERSION/lib
    mkdir $1/cas-json-tool-$VERSION/svn
    mkdir $1/cas-json-tool-$VERSION/git
fi

cp -f target/*.jar $1/cas-json-tool-$VERSION/lib/.

cp -f src/main/resources/cas-json-tool $1/cas-json-tool-$VERSION/bin/cas-json-tool
cp -f src/main/resources/cas-json-to-csv $1/cas-json-tool-$VERSION/bin/cas-json-to-csv
cp -f src/main/resources/cas-json-tool $1/cas-json-tool-$VERSION/bin/cas-json-tool
cp -f src/main/resources/svnProcess $1/cas-json-tool-$VERSION/bin/svnProcess

chmod 755 $1/cas-json-tool-$VERSION/bin/svnProcess
chmod 755 $1/cas-json-tool-$VERSION/bin/cas-json-tool
chmod 755 $1/cas-json-tool-$VERSION/bin/cas-json-to-csv

cd $1/cas-json-tool-$VERSION/lib
ln -s cas-json-tool-$VERSION-jar-with-dependencies.jar cas-json-tool-latest.jar

cd $1
if [ -h cas-json-tool ] ; then
	rm cas-json-tool
fi
ln -s cas-json-tool-$VERSION cas-json-tool

echo ''
echo 'cas-json-tool installation complete!'
echo ''
echo 'Please add the following lines to your .bashrc or .bash_profile'
echo "export CASTOOL_HOME=$1/cas-json-tool"
echo 'export PATH="$PATH:$CASTOOL_HOME/bin"'

