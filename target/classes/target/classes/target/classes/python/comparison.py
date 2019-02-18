#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Aug  1 09:50:19 2018

@author: henryhe
"""
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import numpy as np
countdata = np.zeros((24,36))
savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_adjusted/figures/"
filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/c00/4angles/"
for i in range(0,2):
    countname = filepath+"angularcount"+str(i).zfill(2)+".txt"
    with open(countname,"r") as countstream:
        for line in countstream:
            currentline = line.split(",")
            for j in range(0,len(currentline)):
                countdata[i,j] = currentline[j]


countdata2 = np.zeros((24,36))
filepath2 = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_adjusted/"
for i in range(0,24):
    countname = filepath2+"angularcount/angularcount"+str(i).zfill(4)+".txt"
    with open(countname,"r") as countstream:
        for line in countstream:
            currentline = line.split(",")
            for j in range(0,len(currentline)):
                countdata2[i,j] = currentline[j]

listtoadd = [4,9,18,22]
add = countdata2[listtoadd[0]]
for i in range(len(listtoadd)-1):
    add = np.maximum(countdata2[listtoadd[i+1]],add)
#    add = add + countdata2[listtoadd[i+1]]
#add = np.append(add[-1],add[0:-1])
merge = countdata[1]
maxcover = countdata2[0]
for i in range(23):
    maxcover = np.maximum(countdata[i+1],maxcover)
plt.hold(True)
plt.xlim((0,360))
plt.ylim((0,8000))
pltrange = np.arange(0,360,10)
plt.plot(pltrange,add,'r--',label='Estimation')
plt.plot(pltrange,merge,'g--',label='Experimental merged result')
plt.ylabel('Number of foreground blocks')
plt.xlabel('Angular section within sample')
plt.legend()
print(np.sum(countdata,1)[0])
plt.savefig(savepath+"4anglescomparison_max.pdf",format="pdf",dpi=300)