#!/bin/bash
#Author: Jesimar da Silva Arantes
#Date: 19/10/2017
#Last Update: 21/02/2018

echo 'my ip:'

ip addr | grep 'BROADCAST,MULTICAST,UP,LOWER_UP' -A2 | tail -n1 | awk '{print $2}' | cut -f1  -d'/'
