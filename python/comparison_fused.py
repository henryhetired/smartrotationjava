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
savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_adjusted/figures/"

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
    ax1.legend(loc="upper right")
    ax1.set_xlabel("Angle within sample")
    ax1.set_ylabel("Number of foreground blocks")
    ax1.fill_between(degree,countdata[0],countdata[1],where=countdata[1]>countdata[0],facecolor='green',alpha = 0.05)
    ax1.fill_between(degree,countdata[0],countdata[1],where=countdata[1]<countdata[0],facecolor='red',alpha = 0.05)
    fig.savefig(savepath+"%danglecompare.pdf" %a,format = "pdf",dpi=300,bbox_inches="tight") 
    plt.close(fig)
#    fig = plt.figure()
#    ax2 = plt.subplot(111,polar=False)
#    ax2.tick_params(labelsize=5)
#    ax2.set_theta_zero_location("W")
#    ax2.set_theta_direction(1)
#    theta = np.arange(0,np.pi*2,np.pi/18)
#    ax2.plot(theta,countdata[0],'g',linewidth=linewidth)
#    ax2.plot(theta,countdata[1],'r',linewidth=linewidth)
#    ax2.fill_between(theta,countdata[0],countdata[1],where=countdata[1]>countdata[0],facecolor='green',alpha = 0.05)
#    ax2.fill_between(theta,countdata[0],countdata[1],where=countdata[1]<countdata[0],facecolor='red',alpha = 0.05)
#    ax2.set_rmax(5500)
#    fig.tight_layout()
##    plt.savefig(savepath+"%danglecompare_polar.pdf" %a,format = "pdf",dpi=300,bbox_inches="tight") 
#    plt.close(fig)
    #    ax.set_theta_zero_location("W")
#    ax.set_theta_direction(1)
#    ax.set_rmax(5500)
#    ax.set_yticks([0,4000])
    
blind = np.append(21362,blind)
smart = np.append(27661,smart)
#ax3 = plt.subplot(111)
#ax3.set_xlim((0,5))
#ax3.set_ylim((0,75000))
#ax3.set_xlabel("Number of views")
#ax3.set_ylabel("Number of foreground blocks")
#ax3.plot(np.arange(1,5),blind,'g--',label="Blind multiview")
#ax3.plot(np.arange(1,5),smart,'r--',label="Smart Rotation")
#ax3.set_title("Number of foreground blocks vs number of views")
#ax3.legend()
#fig.tight_layout()
#plt.savefig(savepath+"convergence.pdf",format = "pdf",dpi=500,bbox_inches="tight")