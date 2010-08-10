#!/bin/sh

cd `dirname $0`
java -classpath .:./lib/pircbot.jar org.jibble.logbot.LogBotMain
