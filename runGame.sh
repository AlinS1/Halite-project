#!/bin/bash

javac MyBot.java
javac RandomBot.java
./halite -d "30 30" -s 42 "java MyBot" "bots/DBotv4_linux_x64"
