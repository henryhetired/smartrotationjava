#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Aug  1 09:50:19 2018

@author: henryhe
"""
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib import gridspec
from mpl_toolkits.mplot3d import Axes3D
import numpy as np
countdata = np.zeros((24,36))
savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_final3/figures/"
textfontsize =  14
blind = np.array([])
smart = np.array([])
linewidth = 0.5
for a in range(2,5):
    plt.cla()
    filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/c00/%dangles/"%a
    for i in range(0,2):
        countname = filepath+"angularcount"+str(i).zfill(2)+".txt"
        with open(countname,"r") as countstream:
            for line in countstream:
                currentline = line.split(",")
                for j in range(0,len(currentline)):
                    countdata[i,j] = currentline[j]
    fig = plt.figure()
    ax1=plt.subplot(111,polar = False)
    ax1.set_ylim([0,5500])
    ax1.tick_params(labelsize=8)
    degree = np.arange(0,360,10)
    countdata[0] = np.flip(countdata[0],0)
    countdata[1] = np.flip(countdata[1],0)
    ax1.plot(degree,countdata[0],'g',linewidth = linewidth,label = "Blind multiview",alpha=.5)
    blind = np.append(blind,np.sum(countdata[0]))
    smart = np.append(smart,np.sum(countdata[1]))
    ax1.plot(degree,countdata[1],'r',linewidth = linewidth,label = "Smart Rotation")
    ax1.legend(loc="upper right",fontsize=textfontsize)
    ax1.set_xlabel("Angle within sample",fontsize=textfontsize)
    ax1.set_ylabel("No. foreground Voxels",fontsize=textfontsize)
    ax1.fill_between(degree,countdata[0],countdata[1],where=countdata[1]>countdata[0],facecolor='green',alpha = 0.05)
    ax1.fill_between(degree,countdata[0],countdata[1],where=countdata[1]<countdata[0],facecolor='red',alpha = 0.05)
    plt.xticks(fontsize=12)
    plt.yticks(fontsize=12)
    fig.savefig(savepath+"%danglecompare.pdf" %a,format = "pdf",dpi=300,bbox_inches="tight") 
    plt.close(fig)
