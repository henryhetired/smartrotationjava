#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Feb 13 12:52:46 2019

@author: henryhe
"""

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
textfontsize =  12
blind = np.array([])
smart = np.array([])
linewidth = 0.5
fig = plt.figure(figsize=(4,8.25))
ax3 = plt.subplot(313)
ax1 = plt.subplot(311,sharex=ax3)
ax2 = plt.subplot(312,sharex=ax3)

axes = [ax1,ax2,ax3]
for a in range(2,5):
    filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/c00/%dangles/"%a
    for i in range(0,2):
        countname = filepath+"angularcount"+str(i).zfill(2)+".txt"
        with open(countname,"r") as countstream:
            for line in countstream:
                currentline = line.split(",")
                for j in range(0,len(currentline)):
                    countdata[i,j] = currentline[j]
    axes[a-2].set_ylim([0,5500])
    degree = np.arange(0,360,10)
    countdata[0] = np.flip(countdata[0],0)
    countdata[1] = np.flip(countdata[1],0)
    axes[a-2].plot(degree,countdata[0],'g',linewidth = linewidth,label = "Blind multiview",alpha=.5)
    blind = np.append(blind,np.sum(countdata[0]))
    smart = np.append(smart,np.sum(countdata[1]))
    axes[a-2].plot(degree,countdata[1],'r',linewidth = linewidth,label = "Smart Rotation")
    axes[a-2].fill_between(degree,countdata[0],countdata[1],where=countdata[1]>countdata[0],facecolor='green',alpha = 0.05)
    axes[a-2].fill_between(degree,countdata[0],countdata[1],where=countdata[1]<countdata[0],facecolor='red',alpha = 0.05)
plt.setp(ax1.get_xticklabels(),visible=False)
plt.setp(ax2.get_xticklabels(),visible=False)
ax3.locator_params(nbins=10,axis='x')
fig.tight_layout()
plt.savefig(savepath+"comparisons.pdf",format="pdf",dpi=300)