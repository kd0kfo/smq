#!/bin/sh

echo Testing Python API
cd python
PYTHONPATH=$PWD python setup.py test
RETVAL=$?
cd ..
if [ $RETVAL -ne 0 ];then
	echo Python API Test Failed
	exit $RETVAL
fi

echo Testing Java API
cd java
if [ ! -e lib/sqlite-jdbc-3.7.2.jar ];then
	cd lib
	./downloadDependencies.sh || exit 1
	cd ..
fi
ant test
RETVAL=$?
cd ..
if [ $RETVAL -ne 0 ];then
	echo Java API Test Failed
	exit $RETVAL
fi

echo Unit Test Passed
