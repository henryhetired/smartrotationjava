#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Mar  1 13:34:27 2019

@author: henryhe
"""

#Script to evaluation the response of each angle relative to the imaging angle
import matplotlib.pyplot as plt
import numpy as np
from evaluationstep_final import smart_rotation
filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_final3/angularcount_final/"
sr = smart_rotation(24,10)
sr.evaluate_angles(filepath,24,10,0,False)
savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_final3/figures/"
alpha = 0.2
fig,(a0,a1,a2,a3) = plt.subplots(4,1,sharex = True,figsize=(4.5,3),gridspec_kw={'height_ratios':[12,1,1,1]})
plt.subplots_adjust(left=0,right=1,top=1,bottom=0,hspace=0.03)
linewidth = 1
markersize=2
a0.plot(np.arange(0,360,10),np.divide(sr.a[:,0]/np.pi/2,np.i0(sr.k))*np.exp(np.abs(sr.k))[:,0],'g-o',label="max",lw =linewidth,markersize=markersize)
a0.plot(np.arange(0,360,10),np.divide(sr.a[:,0]/np.pi/2,np.i0(sr.k))*np.exp(-np.abs(sr.k))[:,0],'r-o',label="min",lw=linewidth,markersize=markersize)
a0.grid(False)
a0.legend(loc="upper right")
a0.set_xlim(0,360)
a0.set_ylim(0,15000)
a0.xaxis.tick_top()
#a0.set_xlabel('imaging angle')
a0.set_xlabel('Angle within sample')
axis = [a1,a2]
for i in axis:
    i.set_yticklabels([])
    i.xaxis.set_ticks_position('none')
    i.yaxis.set_ticks_position('none')
    i.set_xlim(0,360)
mini = 1
maxi = 0
for i in range(24):
    angles = np.arange(i,24+i,6)%24
    coverage = sr.estimate_coverage_average(angles)
    if (coverage > maxi):
        winner = angles
        maxi = coverage
    if (coverage<mini):
        loser = angles
        mini = coverage
optimal = sr.get_optimal_coverage(4)
for i in range(len(winner)):
    a1.axvline(loser[i]*15,c = 'r',linewidth = 5)
    a2.axvline(winner[i]*15,c = 'g',linewidth = 5)
    a3.axvline(optimal[i]*15,c = 'orange',linewidth = 5)
a3.set_yticklabels([])
a3.set_xlim(0,360)
a3.yaxis.set_ticks_position('none')
plt.savefig(savepath+"3F.pdf",dpi=500,format = "pdf")