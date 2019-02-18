#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Nov 26 13:21:07 2018

@author: henryhe
"""
import fileinput
filepattern = "/mnt/fileserver/Henry-SPIM/smart_rotation/11222018/e5/data/t%04d_conf%04d_view0000.ome.txt"
s = open(filepattern%(0,1),'r+')
#for i in range(0,32):
#    for j in range(0,24):
#        with fileinput.FileInput(filepattern%(i,j), inplace=True, backup='.bak') as file:
#            for line in file:
#                print(line.replace("t%04d_conf%04d_view%04d.ome.raw", "t%04d_conf%04d_view0000.ome.raw"), end='')
#for i in range(0,32):
#    for j in range(0,24):
#       f = open(filepattern%(i,j),'r')
#       a = ['xypixelsizeum=0.65','zpixelsizeum=4']
#       lst = []
#       for line in f:
#           for word in a:
#               if word in line:
#                   line = line.replace(word,'')
#           lst.append(line)
#       f.close()
#       f = open(filepattern%(i,j),'w')
#       for line in lst:
#           f.write(line)
#       f.close()
for i in range(0,32):
    for j in range(24):
        with open(filepattern%(i,j), 'a') as file:
            file.write('\n')
            file.write("xypixelsizeum=0.65\n")
            file.write("zpixelsizeum=4\n")
            