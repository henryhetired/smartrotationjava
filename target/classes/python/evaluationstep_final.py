#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Dec  6 12:07:05 2018

@author: henryhe
"""

import sys
import matplotlib.pyplot as plt
import numpy as np
import math
from lmfit import Model
angles_used = set()
coverage = np.zeros(1)
a = np.zeros((2, 1))
c = np.zeros((2, 1))
k = np.zeros((2, 1))
fitted_distribution = np.zeros((2,1))
distribution = np.zeros((2, 1))
num_angles = 24
angular_resolution = 15


def vonmises(x, amp, cen, kappa):
    # "1-d vonmises"
    top = (amp/(np.pi*2*np.i0(kappa)))
    bot = np.exp(kappa*np.cos(x/360.0*2.0*np.pi-cen))
    return top*bot


def inv_vonmises(y, amp, cen, kappa):
    first = np.log(y*np.pi*2*np.i0(kappa)/amp)/kappa
    second = math.acos(first)
    result = np.zeros(2)
    result[0] = second+cen
    result[1] = 2*np.pi-second+cen
    return result


def get_cmap(n, name='brg'):
    return plt.cm.get_cmap(name, n)


def evaluate_angles(filepath, num_angles_in, angular_resolution_in,timepoint):
#    read data, evaluate and fit to model, generate distribution map
    global angular_resolution
    global num_angles
    global distribution    
    global coverage
    global a
    global c
    global k
    num_angles = num_angles_in
    angular_resolution = angular_resolution_in
    num_angles_evaluated = 360//angular_resolution
    countdata = np.zeros((num_angles, num_angles_evaluated))
    for i in range(0, num_angles):
        countname = filepath+"angularcount"+str(timepoint).zfill(4)+"_"+str(i).zfill(4)+".txt"
        with open(countname, "r") as countstream:
            for line in countstream:
                currentline = line.split(",")
                for j in range(0, len(currentline)):
                    countdata[i, j] = currentline[j]
    a.resize(num_angles_evaluated, 1)
    c.resize(num_angles_evaluated, 1)
    k.resize(num_angles_evaluated, 1)
    distribution.resize(num_angles_evaluated, num_angles)
    coverage.resize(num_angles_evaluated)
    for i in range(0, num_angles_evaluated):
        r = countdata[:, i]
        gmodel = Model(vonmises)
        x = np.array(range(0, 360, 360//num_angles))
        result = gmodel.fit(r, x=x, amp=r.max(0), cen=i *
                            angular_resolution, kappa=np.pi/4.0)
        a[i] = result.params['amp'].value
        c[i] = result.params['cen'].value
        k[i] = result.params['kappa'].value
        distribution[i] = vonmises(
            np.arange(0, 360, 360//num_angles), a[i], c[i], k[i])
    return

def estimate_coverage_average(angle_array):
    global num_angles
    current_coverage = np.zeros(num_angles)
    for i in range(len(angle_array)):
        current_coverage = np.maximum(current_coverage,distribution[:,angle_array[i]])                
    return(np.mean(current_coverage/np.max(distribution,1)))
def estimate_coverage_global(angle_array):
    current_coverage = np.zeros(num_angles)
    for i in range(len(angle_array)):
        current_coverage = np.maximum(current_coverage,distribution[:,angle_array[i]])                
    return(np.sum(current_coverage))
def get_optimal_coverage(num_angles):
#    given the number of angles used, what combination gives the highest average percentage of maximum
    global distribution
    from itertools import combinations
    comb = list(combinations(range(24),num_angles))
    coverage_percentage = 0
    coverage_sum = np.sum(coverage,0)
    for i in range(len(comb)):
        current_coverage = coverage
        new_coverage_percentage = estimate_coverage_average(comb[i])
        if (new_coverage_percentage>coverage_percentage):
            winner = comb[i]
            coverage_percentage = new_coverage_percentage
    print(winner)
    return(winner)

pathtotext = "/mnt/fileserver/Henry-SPIM/smart_rotation/11222018/e5/data/workspace/"
result = np.zeros((32,4))
coverage_optimal = np.zeros(32)
coverage_standard = np.zeros(32)
for i in range(32):
    timepoint = i
    evaluate_angles(pathtotext, 24, 15,timepoint)
    num_angle = 4
    angles_get = get_optimal_coverage(num_angle)
    result[i] = angles_get
    coverage_optimal[i] = estimate_coverage_average(angles_get)
    coverage_standard[i] = estimate_coverage_average(np.arange(0,num_angles,6))
fig = plt.figure(figsize=(3.4,2.55))
ax = plt.subplot(111)
plt.subplots_adjust(left=0,right=1,bottom=0,top=1)
plt.hold(True)
#plt.xlabel("Time /Hours",fontsize=20)
#plt.ylabel("Imaging angle",fontsize = 20)
#plt.title("Optimal angles over time",fontsize = 20)
plt.xticks(fontsize=10)
plt.yticks(fontsize=10)
for i in range(num_angle):
    ax.plot(np.arange(0,16,0.5),result[:,i]*15,'--',label="angle %02d"%i)
ax.locator_params(nbins=10,axis='x')
plt.savefig(pathtotext+"/figures/timelapse.pdf",format="pdf")
#plt.show()
f, (ax,ax2) = plt.subplots(2,1,sharex=True,gridspec_kw = {'height_ratios':[15, 1]},figsize=(3.4,2.55))
plt.subplots_adjust(left=0,right=1,bottom=0,top=1)
plt.hold(True)
ax.plot(np.arange(32)*0.5,coverage_optimal,'--',label="Smart rotation")
ax.plot(np.arange(32)*0.5,coverage_standard,'--',label="Blind multi-view")
ax2.plot(np.arange(32)*0.5,coverage_optimal,'--',label="Smart rotation")
ax2.plot(np.arange(32)*0.5,coverage_standard,'--',label="Blind multi-view")
ax2.set_ylim([0,0.01])
ax.set_ylim(0.85,1)
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
ax.legend()
ax2.locator_params(nbins=16,axis='x')
#ax.set_title("Imaging coverage over time",fontsize=15)
plt.savefig(pathtotext+"figures/coverage_over_time.pdf",format="pdf")