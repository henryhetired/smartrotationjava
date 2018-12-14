#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Dec  6 13:48:16 2018

@author: henryhe
"""

from ggplot import *
import numpy as np
import pandas as pd
import math
from lmfit import Model
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import axes3d, Axes3D
#list of global variables

angles_used = set()
coverage = np.zeros(1)
a = np.zeros((2, 1))
c = np.zeros((2, 1))
k = np.zeros((2, 1))
fitted_distribution = np.zeros((2,1))
distribution = np.zeros((2, 1))
num_angles = 24
angular_resolution = 15

def get_cmap(n, name='brg'):
    '''Returns a function that maps each index in 0, 1, ..., n-1 to a distinct 
    RGB color; the keyword argument name must be a standard mpl colormap name.'''
    return plt.cm.get_cmap(name, n)
    
cmap = get_cmap(24,'brg');
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
pathtotext = "/mnt/fileserver/Henry-SPIM/smart_rotation/11222018/e5/data/workspace/"
#for tp in range(32):
#    evaluate_angles(pathtotext,num_angles,angular_resolution,tp)
#    df = pd.DataFrame(columns=["Imaging angle","Angular slices","Number"])
#    for i in range(len(distribution)):
#        for j in range(len(distribution[i])):
#            df.loc[i*len(distribution)+j] = [j*15,i*15,distribution[i][j]]
#    df.to_csv("~/distribution%02d.csv"%tp)

fig = plt.figure()
for tp in range(32):
#    fig.clear()
    ax = fig.gca()
    x = np.arange(0,360,15)
    plt.hold(True)
    evaluate_angles(pathtotext,num_angles,angular_resolution,tp)
    displace = 4000
    for slice in range(24):
        if (tp==0):
            ax.plot(x,distribution[slice]+slice*displace,alpha=0.1,color=cmap(slice),label="%02d$\circ$"%(slice*angular_resolution))
        else:
            ax.plot(x,distribution[slice]+slice*displace,alpha=0.1,color=cmap(slice))
    ax.set_yticks([])
    ax.set_ylim(0,110000)
    ax.grid(False)
    ax.set_xlabel("Imaging angle/degrees")
    ax.set_title("Ridge plot of angular imaging response")
    plt.legend(bbox_to_anchor=(1.1,1),prop={'size':3.8})
    plt.hold(False)
plt.savefig(pathtotext+"figures/rainbow/tp%02d.pdf"%33,dpi = 500,format = "pdf",bbox_inches="tight")
plt.show()
