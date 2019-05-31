#!/bin/bash
find -name "*.java" > sources.txt
javac -d ./out @sources.txt
rm sources.txt
