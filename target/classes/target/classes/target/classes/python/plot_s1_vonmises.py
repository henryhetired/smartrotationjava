#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Jul  2 21:23:24 2018

@author: henryhe
"""

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Jun 18 12:44:36 2018

@author: root
"""
#Script to evaluation the response of each angle relative to the imaging angle
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import numpy as np
from lmfit import Model,Parameters
def vonmises(x, amp, cen, kappa):
#    "1-d vonmises"
    return (amp/(np.pi*2*np.i0(kappa))) * np.exp(kappa*np.cos(x/360.0*2.0*np.pi-cen/360*np.pi*2))
def get_cmap(n, name='brg'):
    '''Returns a function that maps each index in 0, 1, ..., n-1 to a distinct 
    RGB color; the keyword argument name must be a standard mpl colormap name.'''
    return plt.cm.get_cmap(name, n)
num_angle_in=24
angular_resolution = 10
num_angle_out = 360//angular_resolution
cmap = get_cmap(num_angle_out,'brg');  
countdata = np.zeros((num_angle_in,num_angle_out))
filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_final3/angularcount_final/"
timepoint = 2
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
fig = plt.figure(figsize=(6,4))
for i in range(0,num_angle_out):
    print(i)
#    plt.cla();
    patches.append(mpatches.Patch(color=cmap(i),label='',alpha = 0.3))
    name = savepath+savename+"_%04d"%i+".pdf"
    r = np.flip(countdata[:,i],0)
    gmodel = Model(vonmises)
    x = np.array(np.arange(0,360,360/num_angle_in))
    params = Parameters()
    params.add('amp',r.max(0))
    params.add('cen',i*angular_resolution)
    params.add('kappa',np.pi/4.0)    
    result = gmodel.fit(r,params,x=x)
    a[i] = result.params['amp'].value
    c[i] = result.params['cen'].value
    k[i] = result.params['kappa'].value
#    print(c[i])  
    plotdata = vonmises(np.arange(0,360,angular_resolution),a[i],c[i],k[i])
        
    plt.hold(True)
    plt.plot(np.arange(0,360,angular_resolution),plotdata,color = cmap(i),label='Fitted data',alpha = 0.5)
    plt.plot(np.arange(0,360,360/num_angle_in),r,'+',color=cmap(i),alpha=0.5,label='Observation')
    plt.legend(fontsize=7,loc = 1)
    plt.xlim((0,360))
    plt.ylim((0,14000))
    plt.legend(handles = patches,bbox_to_anchor=(1.08,1),labelspacing = 0.01,fontsize = 5,frameon=False)
#    s = r'$ %03d^{\circ}$'%(angular_resolution*i)
    plt.title("Imaging response curve of anglular slice")
    plt.xlabel("Imaging angle")
    plt.ylabel("number of foreground blocks")
#    plt.legend(fontsize = 7)
plt.savefig(name,dpi = 500,format = "pdf",bbox_inches="tight")  
plt.cla();
#plt.plot(range(0,360,angular_resolution),np.max(countdata,1))
#plt.title("Angular maximum information content vs angle")
#plt.xlim((0,360))
#plt.ylim((0,15000))
#plt.savefig(savepath+"amplitude vs angle at tp "+str(timepoint).zfill(2)+".pdf",dpi = 500,format = "pdf",bbox_inches="tight")    
#plt.cla()
plt.title("estimated vs theoretical optimal angle")
#errorbar = np.arccos(np.log(np.cosh(k)/k))
errorbar = (np.arccos(np.log(np.exp(np.abs(k))/2)/np.abs(k)))/np.pi*180*2
errorbar = np.deg2rad(errorbar)
errorbar = np.rad2deg((2*np.pi + errorbar) * (errorbar < 0) + errorbar*(errorbar > 0))
for i in range(len(k)):
    if (k[i]<0):
        c[i] = c[i]+180
    c[i] = c[i]%360-45
#    if np.abs(c[i] - i*(360/len(k)))>np.abs(360-c[i]-i*(360/len(k))):
#        c[i] = 360-c[i]
#c = c[::-1]%360
#plt.errorbar(range(0,360,10),c,errorbar,label="Measured",color='red',fmt='*')
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
plt.legend(fontsize=7,loc="upper left")
plt.savefig(savepath+"optimal vs imaging angle"+".pdf",dpi = 500,format = "pdf",bbox_inches="tight")    

