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

filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_test/"
for i in range(0,24):
    countname = filepath+"/angularcount/angularcount"+str(i).zfill(4)+".txt"
    with open(countname,"r") as countstream:
        for line in countstream:
            currentline = line.split(",")
            for j in range(0,len(currentline)):
                countdata[i,j] = currentline[j]
savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_test/figures/"
savename = "information_content"                     
for idx in range(0,24):
    print(idx)
    name = savepath+savename+str(idx).zfill(3)+".pdf"
    normalized = np.flip(countdata[idx,:],0)
    maxidx = np.argmax(countdata[idx,:])
    plt.cla()
    x = range(0,360,10)
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
    theta = np.linspace(0.0,2*np.pi,36,endpoint=False)

    ax.bar(theta,normalized,width=2*np.pi/36,color='g',alpha = 0.2)
    
    newrange = np.linspace(0,360,360);
    datasmooth = spline(range(0,360,10),normalized,newrange)
    indexes = peakutils.indexes(normalized,min_dist = 12)
#    ax.legend(handles=[blue_patch,green_patch],bbox_to_anchor=(1.46,1))
    ax.hold(True)
    
#    for i in range(0,len(indexes)):
#        left_index = indexes[i]-window_size if indexes[i]>window_size else 0
#        right_index = indexes[i]+window_size if indexes[i]+window_size<len(normalized) else len(normalized)
#        data = normalized[left_index:right_index]
#        gmodel = Model(gaussian)
#        x = np.array(range(left_index,right_index))*10
#        result = gmodel.fit(data,x = x,amp = 10000,cen = indexes[i]*10,wid = window_size*2)
##        print result.fit_report()
#        a = result.params['amp'].value
#        m = result.params['cen'].value
#        u = result.params['wid'].value
#        plot_data = gaussian(range(0,360,10),a,m,u)
#        ax.plot(theta,plot_data,alpha = 0.3,color = 'r' )
#        print(str(a) + ",  "+str(m)+",  "+str(u) +",  "+ str(m+2.3548*u/2) + ", "+str(m-2.3548*u/2))
    ax.set_rmax(14000)
    plt.savefig(name,dpi = 500,format = "pdf",bbox_inches="tight")
