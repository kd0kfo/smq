#!/bin/sh

pip list|grep pycoss

if [ $? != 0 ];then
	if [ ! -e dependencies ];then
		mkdir dependencies
	fi
	cd dependencies
	git clone https://github.com/kd0kfo/pycoss.git
	cd pycoss
	pip install $PWD
	cd ../..
fi
