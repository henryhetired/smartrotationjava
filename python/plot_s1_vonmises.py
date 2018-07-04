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
from scipy.stats import vonmises
from scipy.optimize import curve_fit
from sklearn.preprocessing import normalize
from scipy.interpolate import spline
import scipy.special
from lmfit import Model,Parameters
from scipy.interpolate import UnivariateSpline
def vonmises(x, amp, cen, kappa):
#    "1-d vonmises"
    return (amp/(np.pi*2*np.i0(kappa))) * np.exp(kappa*np.cos(x/360.0*2.0*np.pi-cen))
def get_cmap(n, name='brg'):
    '''Returns a function that maps each index in 0, 1, ..., n-1 to a distinct 
    RGB color; the keyword argument name must be a standard mpl colormap name.'''
    return plt.cm.get_cmap(name, n)
cmap = get_cmap(36,'brg');  
countdata = np.zeros((24,36))

avgdata = np.zeros((24,36))

filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/downsampled4x/workspace"
for i in range(0,24):
    countname = filepath+"/angularcount/angularcount"+str(i).zfill(4)+".txt"
    with open(countname,"r") as countstream:
        for line in countstream:
            currentline = line.split(",")
            for j in range(0,len(currentline)):
                countdata[i,j] = currentline[j]
#savepath = "Z:\\Henry-SPIM\\11132017\\e2\\t0000\\analysis\\analysis5 angular_plot\\figures\\plot 3_1\\"
savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/downsampled4x/workspace/figures/"
savename = "information_content_fitted"  

a = np.zeros((36,1))
c = np.zeros((36,1))
k = np.zeros((36,1))
patches = []
for i in range(0,36):
    print(i)
#    plt.cla();
    patches.append(mpatches.Patch(color=cmap(i),label='',alpha = 0.5))
    name = savepath+savename+"%04d"%i+".pdf"
    r = countdata[:,i]
    gmodel = Model(vonmises)
    x = np.array(range(0,360,15))
    params = Parameters()
    params.add('amp',r.max(0))
    params.add('cen',i*10)
    params.add('kappa',np.pi/4.0)    
    result = gmodel.fit(r,params,x=x)
    a[i] = result.params['amp'].value
    c[i] = result.params['cen'].value
    k[i] = result.params['kappa'].value
#    print(c[i])  
    plotdata = vonmises(np.arange(0,360,15),a[i],c[i],k[i])
    plt.hold(True)
    plt.plot(np.arange(0,360,15),plotdata,color = cmap(i),label='Fitted data',alpha = 0.5)
    plt.plot(np.arange(0,360,15),r,'+',color=cmap(i),alpha=0.5,label='Observation')
#    plt.legend(fontsize=7,loc = 1)
    plt.xlim((0,360))
    plt.ylim((0,14000))
#    plt.legend(handles = patches,bbox_to_anchor=(1.1,1),labelspacing = 0.01,fontsize = 5,frameon=False)
#    s = r'$ %03d^{\circ}$'%(360-10*i)
    plt.title("Imaging response curve of anglular slice")
    plt.xlabel("Imaging angle")
    plt.ylabel("number of foreground blocks")
#    plt.legend(fontsize = 7)
plt.savefig(name,dpi = 500,format = "pdf",bbox_inches="tight")
plt.cla();
plt.plot(range(0,360,10),a)
plt.title("Angular maximum information content vs angle")
plt.xlim((0,360))
plt.ylim((0,35000))
plt.savefig(savepath+"amplitude vs angle.pdf",dpi = 500,format = "pdf",bbox_inches="tight")    
plt.cla()
plt.title("estimated optimal angle vs theoretical optimal angle")
k=np.abs(k)
#errorbar = np.arccos(np.log(np.cosh(k)/k))
errorbar = (np.arccos(np.log(np.exp(np.abs(k))/2)/k))/np.pi*180*2
plt.errorbar(range(0,360,10),c,errorbar,label="Measured",color='red',fmt='*')
plt.hold(True)
plt.plot(range(0,360,10),range(0,360,10),'b',label="Theoretical")
plt.legend(fontsize=7)
plt.savefig(savepath+"optimal angle vs imaging angle.pdf",dpi = 500,format = "pdf",bbox_inches="tight")    

