#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Jun  6 11:31:28 2018

@author: henryhe
"""

# -*- coding: utf-8 -*-
"""
Created on Tue Feb 27 10:36:13 2018

@author: jhe1
"""

# -*- coding: utf-8 -*-
"""
Created on Mon Jan 22 14:40:43 2018

@author: jhe1
"""

# -*- coding: utf-8 -*-
"""
Created on Mon Jan 22 13:19:46 2018

@author: jhe1
"""

# -*- coding: utf-8 -*-
"""
Created on Mon Dec 25 17:05:49 2017

@author: jhe1
"""

# -*- coding: utf-8 -*-
"""
Created on Mon Dec 25 14:26:31 2017

@author: jhe1
"""

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.mlab as mlab
import matplotlib.patches as mpatches
import csv
from scipy.optimize import curve_fit
from sklearn.preprocessing import normalize
from scipy.interpolate import spline
from scipy.stats import norm
import peakutils
from lmfit import Model

def gaussian(x, amp, cen, wid):
    "1-d gaussian: gaussian(x, amp, cen, wid)"
    return (amp/(np.sqrt(2*np.pi)*wid)) * np.exp(-(x-cen)**2 /(2*wid**2))
window_size = 5

countdata = np.zeros((24,36))

avgdata = np.zeros((24,36))

filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/downsamplez/downsampled8x/workspace"
for i in range(0,24):
    countname = filepath+"/angularcountcumulative/angularcountcumulative"+str(i).zfill(4)+".txt"
    with open(countname,"r") as countstream:
        for line in countstream:
            currentline = line.split(",")
            for j in range(0,len(currentline)):
                countdata[i,j] = currentline[j]
#savepath = "Z:\\Henry-SPIM\\11132017\\e2\\t0000\\analysis\\analysis5 angular_plot\\figures\\plot 3_1\\"
savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/downsamplez/downsampled8x/workspace/figures/"
savename = "information_content_full_cumu"                       
def get_cmap(n, name='brg'):
    '''Returns a function that maps each index in 0, 1, ..., n-1 to a distinct 
    RGB color; the keyword argument name must be a standard mpl colormap name.'''
    return plt.cm.get_cmap(name, n)
    
cmap = get_cmap(24,'hsv');
patches = []
for idx in range(0,24):
    print(idx)
    name = savepath+savename+str(idx).zfill(4)+".pdf" 
#    patches.append(mpatches.Patch(color=cmap(idx),label='',alpha = 0.5))
    normalized = countdata[idx,:]
    maxidx = np.argmax(countdata[idx,:])
    plt.cla()
    x = range(0,360,10)
    x = np.array(x)
    ax = plt.subplot(111,polar=True)
    ax.set_theta_zero_location("W")
    ax.set_theta_direction(-1)
    ax.set_rmax(25000)
    ax.set_yticks(np.linspace(0,25000,4,endpoint=False))
    theta = np.linspace(0.0,2*np.pi,36,endpoint=False)
#    ax.bar(theta,normalized,width=2*np.pi/36,color=cmap(idx),alpha = 0.1)
    ax.bar(theta,normalized,width=2*np.pi/36,color='r',alpha = 0.2)
    newrange = np.linspace(0,360,360);
    datasmooth = spline(range(0,360,10),normalized,newrange)
    indexes = peakutils.indexes(normalized,min_dist = 12)
#    ax.hold(True)
    ax.set_rmax(25000)
#    ax.legend(handles = patches,bbox_to_anchor=(1.2,1),labelspacing = 0.01,fontsize = 5,frameon=False)
    plt.savefig(name,dpi = 500,format = "pdf",bbox_inches="tight")

