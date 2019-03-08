#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Feb 14 11:57:06 2019

@author: henryhe
"""
#Script to evaluation the response of each angle relative to the imaging angle
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import numpy as np
from lmfit import Model,Parameters
import Utils as ut
num_angle_in=24
angular_resolution = 10
num_angle_out = 360//angular_resolution
cmap = ut.get_cmap(num_angle_out,'brg');  
countdata = np.zeros((num_angle_in,num_angle_out))
filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_final3/angularcount_final/"
for i in range(0,num_angle_in):
    countname = filepath+"angularcount%04d.txt"%i
    with open(countname,"r") as countstream:
        for line in countstream:
            currentline = line.split(",")
            for j in range(0,len(currentline)):
                countdata[i,j] = currentline[j]
savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_final3/figures/"
savename = "information_content_fitted"  

a = np.zeros((num_angle_out,1))
c = np.zeros((num_angle_out,1))
k = np.zeros((num_angle_out,1))
patches = []
f, (ax,ax2) = plt.subplots(2,1,sharex=True,figsize=(4.5,3))
plt.subplots_adjust(left=0,right=1,top=1,bottom=0)
name = savepath+savename+"full.pdf"
for i in range(0,num_angle_out):
    patches.append(mpatches.Patch(color=cmap(i),label='',alpha = 0.3))
    r = np.flip(countdata[:,i],0)
    gmodel = Model(ut.vonmises)
    x = np.array(np.arange(0,360,360/num_angle_in))
    params = Parameters()
    params.add('amp',r.max(0))
    params.add('cen',i*angular_resolution)
    params.add('kappa',np.pi/4.0)    
    result = gmodel.fit(r,params,x=x)
    a[i] = result.params['amp'].value
    c[i] = result.params['cen'].value
    k[i] = result.params['kappa'].value
    plotdata = ut.vonmises(np.arange(0,360,angular_resolution),a[i],c[i],k[i])
    plotdata_theory = ut.vonmises(np.arange(0,360,angular_resolution),23000,np.arange(0,360,10)[i]+45,np.pi/2)
    plt.hold(True)
    ax2.plot(np.arange(0,360,angular_resolution),plotdata,color = cmap(i),alpha = 0.5)
    ax.plot(np.arange(0,360,angular_resolution),plotdata_theory,color = cmap(i),alpha = 0.5)
#    ax2.plot(np.arange(0,360,360/num_angle_in),r,'+',color=cmap(i),alpha=0.5)
#    plt.legend(fontsize=7,loc = 1)
    ax.set_xlim((0,360))
    ax.set_ylim((0,14000))
    ax2.set_xlim((0,360))
    ax2.set_ylim((0,14000))
    plt.legend(handles = patches,bbox_to_anchor=(1.08,1),labelspacing = 0.01,fontsize = 5,frameon=False)
    ax.set_title("Imaging response curve of anglular slice")
    ax2.set_xlabel("Imaging angle")
    ax.set_ylabel("number of foreground blocks")
#    plt.legend(fontsize = 7)
#plt.savefig(name,dpi = 500,format = "pdf")  
plt.cla();
fig = plt.figure(figsize=(4.5,3))
plt.subplots_adjust(left=0,right=1,top=1,bottom=0)
plt.title("estimated vs theoretical optimal angle")
errorbar = (np.arccos(np.log(np.exp(np.abs(k))/2)/np.abs(k)))/np.pi*180*2
errorbar = np.deg2rad(errorbar)
errorbar = np.rad2deg((2*np.pi + errorbar) * (errorbar < 0) + errorbar*(errorbar > 0))
for i in range(len(k)):
    if (k[i]<0):
        c[i] = c[i]+180
    c[i] = c[i]%360-45 #45 degree between ill and detction
c = np.deg2rad(c)
c = np.rad2deg((2*np.pi + c) * (c < 0) + c*(c > 0))
plt.plot(range(0,360,angular_resolution),c,'ro',label="Measured")
plt.fill_between(range(0,360,angular_resolution),c.flatten()-errorbar.flatten()/2,c.flatten()+errorbar.flatten()/2,color='g',alpha=0.2)
plt.hold(True)
plt.xlabel("Angle within sample")
plt.ylabel("Optimal imaging angle")
plt.plot(range(0,360,angular_resolution),range(0,360,angular_resolution),'b',label="Theoretical")
plt.xlim((-10,360))
plt.ylim((-100,430))
plt.legend(fontsize=10,loc="upper left")
#plt.savefig(savepath+"optimal vs imaging angle"+".pdf",dpi = 500,format = "pdf")    
alpha = 0.2
fig = plt.figure(figsize=(4.5,3))
plt.subplots_adjust(left=0,right=1,top=1,bottom=0)
plt.plot(np.arange(0,360,15),np.mean(countdata,1))
for i in np.arange(45,360,90):
    plt.axvline(x=i,linestyle='--',c='g',alpha=alpha)
plt.grid(False)
plt.xlim(0,360)
plt.ylim(0,5000)
plt.xlabel('imaging angle')
plt.ylabel('Average Number of foreground block')
plt.savefig(savepath+"Optical image coverage blind"+".pdf",dpi = 500,format = "pdf") 