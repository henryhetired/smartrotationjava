#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Jul  3 22:50:00 2018

@author: henryhe
"""

import numpy as np
import matplotlib.pyplot as plt
from matplotlib import cm
from mpl_toolkits.mplot3d import Axes3D
def gaussian(x, mu, sig):
    return np.exp(-np.power(x - mu, 2.) / (2 * np.power(sig, 2.)))
def correct_ang_dist(angle,base_angle):
    return (np.min([abs(angle-base_angle),abs(angle-np.pi*2-base_angle),abs(2*np.pi-base_angle+angle)]))
    
base_angle = np.pi/4
fig = plt.figure()
ax = Axes3D(fig)
rad = np.linspace(0, 5, 100)
azm = np.linspace(0, 2 * np.pi, 100)
r, th = np.meshgrid(rad, azm)
th_dist = th
for i in range(len(th)):
    for j in range(len(th[i])):
        th_dist[i][j] = correct_ang_dist(th_dist[i][j],base_angle)
        #checked
density = gaussian(th_dist,0,1)
r, th = np.meshgrid(rad, azm)
ax2 = plt.subplot(projection="polar")
ax2.set_theta_zero_location("W")
ax2.set_theta_direction(-1)
ax2.set_yticklabels([])
plt.pcolormesh(th, r, density,cmap='viridis',alpha = 0.8)
plt.plot(azm, r, color='r', ls='none') 
#plt.savefig("/mnt/fileserver/Henry-SPIM/11132017/e2/analysis/1a.eps",format="eps",dpi=300)

plt.show()

