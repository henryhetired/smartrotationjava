# -*- coding: utf-8 -*-
"""
Created on Mon Dec 25 14:26:31 2017

@author: jhe1
"""
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches


def gaussian(x, amp, cen, wid):
    "1-d gaussian: gaussian(x, amp, cen, wid)"
    return (amp/(np.sqrt(2*np.pi)*wid)) * np.exp(-(x-cen)**2 /(2*wid**2))

window_size = 5

countdata = np.zeros((24,36))

avgdata = np.zeros((24,24))
filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_final3/"
for i in range(0,24):
    countname = filepath+"angularcount_final/angularcount%04d.txt"%i
    with open(countname,"r") as countstream:
        for line in countstream:
            currentline = line.split(",")
            for j in range(0,len(currentline)):
                countdata[i,j] = currentline[j]
savepath = filepath+"figures/angular_content/"
savename = "information_content_"     
fig = plt.figure(figsize=(1.8,1.8))    
plt.subplots_adjust(left=0,right=1,bottom=0,top=1)          
for idx in range(0,24):
    print(idx)
    plt.cla()
    name = savepath+savename+str(idx).zfill(3)+".pdf"
    normalized = countdata[idx,:]
    maxidx = np.argmax(countdata[idx,:])
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
    ax.bar(-theta,normalized,width=2*np.pi/36,color='g',alpha = 0.2)
    ax.tick_params(axis='y',labelsize=0,labelcolor='w')
    plt.xticks(visible=False)
    
    ax.hold(True)  
    ax.set_rmax(14000)

    plt.savefig(name,dpi = 500,format = "pdf")
