# -*- coding: utf-8 -*-
"""
Created on Mon Dec 25 14:26:31 2017

@author: jhe1
"""
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.mlab as mlab
import matplotlib.patches as mpatches
from scipy.optimize import curve_fit
from sklearn.preprocessing import normalize
from scipy.interpolate import spline
from scipy.stats import norm
from lmfit import Model

def gaussian(x, amp, cen, wid):
    "1-d gaussian: gaussian(x, amp, cen, wid)"
    return (amp/(np.sqrt(2*np.pi)*wid)) * np.exp(-(x-cen)**2 /(2*wid**2))
window_size = 5

countdata = np.zeros((24,24))

avgdata = np.zeros((24,24))
timepoint = 1
filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/11222018/e5/data/workspace/"
for i in range(0,24):
    countname = filepath+"angularcount"+str(timepoint).zfill(4)+"_"+str(i).zfill(4)+".txt"
    with open(countname,"r") as countstream:
        for line in countstream:
            currentline = line.split(",")
            for j in range(0,len(currentline)):
                countdata[i,j] = currentline[j]
savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/11222018/e5/data/workspace/figures/"
savename = "information_content_"+str(timepoint).zfill(4)+"_"                    
for idx in range(0,24):
    print(idx)
    name = savepath+savename+str(idx).zfill(3)+".pdf"
    normalized = countdata[idx,:]
    maxidx = np.argmax(countdata[idx,:])
    plt.cla()
    x = range(0,360,15)
    x = np.array(x)
    ax = plt.subplot(111,polar=True)
    ax.set_theta_zero_location("W")
    ax.set_theta_direction(1)
    ax.set_rmax(14000)
    ax.set_yticks(np.linspace(0,10000,4,endpoint=False))
    ax.arrow((idx*15.0)%360.0/180.0*np.pi,14000,0,-1000,head_width = 5.0/180*np.pi,head_length = 900,fc ='b',ec='b',alpha=0.5)
    ax.arrow((idx*15+90.0)%360.0/180.0*np.pi,12000,0,1000,head_width = 5.0/180*np.pi,head_length = 900,fc ='g',ec='g',alpha = 0.5)
    blue_patch = mpatches.Patch(color='blue',label='Illumination',alpha = 0.5)
    green_patch = mpatches.Patch(color='green',label='Detection',alpha = 0.5)
    theta = np.linspace(0.0,2*np.pi,24,endpoint=False)
    ax.bar(theta,normalized,width=2*np.pi/36,color='g',alpha = 0.2)
    ax.hold(True)  
    ax.set_rmax(14000)
#    plt.savefig(name,dpi = 500,format = "pdf",bbox_inches="tight")
