#!/bin/sh

echo Testing Python API
cd python
PYTHONPATH=$PWD python setup.py test
RETVAL=$?
cd ..
if [[ $RETVAL ]];then
	echo Python API Test Failed
	exit $RETVAL
fi
