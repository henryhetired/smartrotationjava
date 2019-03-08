#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Dec  6 12:07:05 2018

@author: henryhe
"""

import matplotlib.pyplot as plt
import numpy as np
from smart_rotation import smart_rotation

pathtotext = "/mnt/fileserver/Henry-SPIM/smart_rotation/11222018/e5/data/workspace/"
result1 = np.zeros((32,4))
result2 = np.zeros((32,3))
coverage_sm4 = np.zeros(32)
coverage_blind4 = np.zeros(32)
coverage_sm3 = np.zeros(32)
coverage_blind3 = np.zeros(32)
for i in range(32):
    print(i)
    timepoint = i
    sr = smart_rotation(24,15,15)
    sr.evaluate_angles(pathtotext,timepoint,usetimepoint=True)
    num_angle = 4
    angles_get = sr.get_optimal_coverage(num_angle)
    result1[i] = angles_get
    coverage_sm4[i] = sr.estimate_coverage_average(angles_get)
    coverage_blind4[i] = sr.estimate_coverage_average(np.arange(0,24,int(24/num_angle)))
    num_angle = 3
    angles_get = sr.get_optimal_coverage(num_angle)
    result2[i] = angles_get
    coverage_sm3[i] = sr.estimate_coverage_average(angles_get)
    coverage_blind3[i] = sr.estimate_coverage_average(np.arange(0,24,int(24/num_angle)))
fig = plt.figure(figsize=(3.4,2.55))
ax = plt.subplot(111)
plt.subplots_adjust(left=0,right=1,bottom=0,top=1)
#plt.xlabel("Time /Hours",fontsize=20)
#plt.ylabel("Imaging angle",fontsize = 20)
#plt.title("Optimal angles over time",fontsize = 20)
plt.xticks(fontsize=10)
plt.yticks(fontsize=10)
for i in range(num_angle):
    ax.plot(np.arange(0,16,0.5),result1[:,i]*15,'--',label="angle %02d"%i)
ax.locator_params(nbins=10,axis='x')
#plt.savefig(pathtotext+"/figures/timelapse.pdf",format="pdf")
plt.show()
f, (ax,ax2) = plt.subplots(2,1,sharex=True,gridspec_kw = {'height_ratios':[15, 1]},figsize=(3.4,2.55))
plt.subplots_adjust(left=0,right=1,bottom=0,top=1)
num_angle = 4
ax.plot(np.arange(32)*0.5,coverage_sm4,'--',label="S%d"%(num_angle),alpha = 0.2)
ax.plot(np.arange(32)*0.5,coverage_blind4,'--',label="B%d"%(num_angle))
ax2.plot(np.arange(32)*0.5,coverage_sm4,'--',label="S%d"%(num_angle),alpha = 0.2)
ax2.plot(np.arange(32)*0.5,coverage_blind4,'--',label="b%d"%(num_angle))
num_angle = 3
ax.plot(np.arange(32)*0.5,coverage_sm3,'--',label="S%d"%(num_angle))
ax.plot(np.arange(32)*0.5,coverage_blind3,'--',label="B%d"%(num_angle),alpha = 0.2)
ax2.plot(np.arange(32)*0.5,coverage_sm3,'--',label="S%d"%(num_angle))
ax2.plot(np.arange(32)*0.5,coverage_blind3,'--',label="B%d"%(num_angle),alpha = 0.2)
ax2.set_ylim([0,0.01])
ax.set_ylim(0.8,1)
ax.spines['bottom'].set_visible(False)
ax2.spines['top'].set_visible(False)
ax.xaxis.tick_top()
ax2.tick_params(labeltop='off')
ax2.xaxis.tick_bottom()
d = .015  # how big to make the diagonal lines in axes coordinates
# arguments to pass to plot, just so we don't keep repeating them
kwargs = dict(transform=ax.transAxes, color='k', clip_on=False)
ax.plot((-d, +d), (-d, +d), **kwargs)        # top-left diagonal
ax.plot((1 - d, 1 + d), (-d, +d), **kwargs)  # top-right diagonal

kwargs.update(transform=ax2.transAxes)  # switch to the bottom axes
ax2.plot((-d, +d), (1 - d, 1 + d), **kwargs)  # bottom-left diagonal
ax2.plot((1 - d, 1 + d), (1 - d, 1 + d), **kwargs)  # bottom-right diagonal
#ax2.set_xlabel("Time /Hours",fontsize=15)
#ax.set_ylabel("Coverage percentage",fontsize=15)
ax.legend(loc = "lower left",ncol=2)
ax2.locator_params(nbins=16,axis='x')
ax.set_title("Imaging coverage over time",fontsize=15)
plt.savefig(pathtotext+"figures/coverage_over_time_compare.pdf",format="pdf")