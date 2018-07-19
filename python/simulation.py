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
global a_new
global c_new
global k_new
savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace/figures/"
def vonmises(x, amp, cen, kappa):
#    "1-d vonmises"
    return (amp/(np.pi*2*np.i0(kappa))) * np.exp(kappa*np.cos(x-cen))
def uniform_density(numangles):
    global savepath
    global k_new
    fig = plt.figure()
    ax = Axes3D(fig)
    rad = np.linspace(0, 5, 500)
    azm = np.linspace(0, 2 * np.pi, 500)
    r, th = np.meshgrid(rad, azm)
    a = np.ones(500)
    a = a/np.linalg.norm(a)
    density = vonmises(th,a[0],0,np.mean(k_new))
    
    for i in range(numangles-1):
        temp=vonmises(th,a[i],(i+1)*np.pi*2/numangles,np.mean(k_new))
        density = np.maximum(temp,density)
    ax2 = plt.subplot(projection="polar")
    ax2.set_theta_zero_location("W")
    ax2.set_theta_direction(1)
    ax2.set_yticklabels([])
    plt.pcolormesh(th, r, density,cmap='viridis',vmin=0)
    plt.plot(azm, r, color='r', ls='none') 
    plt.colorbar(pad=0.1)
    ax2.set_title("Theoretical relative imaging response",y=1.1)
    plt.savefig(savepath+"uniform_response.pdf",format="pdf",dpi=300)
    plt.show() 
    return((np.min(density),np.max(density)))
def get_real_data():
    from scipy.interpolate import interp1d,CubicSpline
    from lmfit import Model,Parameters
    countdata = np.zeros((24,36))
    filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace"
    for i in range(0,24):
        countname = filepath+"/angularcount/angularcount"+str(i).zfill(4)+".txt"
        with open(countname,"r") as countstream:
            for line in countstream:
                currentline = line.split(",")
                for j in range(0,len(currentline)):
                    countdata[i,j] = currentline[j]
    a = np.zeros(36)
    c = np.zeros(36)
    k = np.zeros(36)
    x = np.array(range(0,360,15))/360*np.pi*2
    for i in range(0,36):
        r = countdata[:,i]
        gmodel = Model(vonmises)
        params = Parameters()
        params.add('amp',r.max(0))
        params.add('cen',i*10/360*np.pi*2)
        params.add('kappa',np.pi/4.0)    
        result = gmodel.fit(r,params,x=x)
        a[i] = result.params['amp'].value
        c[i] = result.params['cen'].value
        k[i] = result.params['kappa'].value
    a = a/np.max(a)
    for i in range(len(k)):
        if (k[i]<0):
            c[i] = c[i]+np.pi/2
        c[i] = c[i]%(np.pi*2)
    c = c[::-1]%(np.pi*2)
    k = np.abs(k)
#    method = 'slinear'
#    fa = interp1d(np.linspace(0,np.pi*2,36),a,method,fill_value='extrapolate')
#    fc = interp1d(np.linspace(0,np.pi*2,36),c,method,fill_value='extrapolate')
#    fk = interp1d(np.linspace(0,np.pi*2,36),k,method,fill_value='extrapolate')
    a[0]=a[-1]
    c[0]=c[-1]
    k[0]=k[-1]
    fa = CubicSpline(np.linspace(0,np.pi*2,36),a,bc_type='periodic')
    fc = CubicSpline(np.linspace(0,np.pi*2,36),c,bc_type='periodic')
    fk = CubicSpline(np.linspace(0,np.pi*2,36),k,bc_type='periodic')
    azm = np.linspace(0,2*np.pi,500)
    global a_new
    a_new = fa(azm)
    a_new = a_new/np.linalg.norm(a_new)
    global c_new
    c_new = fc(azm)
    global k_new
    k_new = fk(azm)
def plot_real_data(num_angles,vmin,vmax):
    global a_new
    global c_new
    global k_new
    fig = plt.figure()
    ax = Axes3D(fig)
    rad = np.linspace(0, 5, 500)
    azm = np.linspace(0, 2 * np.pi, 500)
    r, th = np.meshgrid(rad, azm)
    
    density = np.zeros((len(th),len(th[0])))
    temp = density
    for i in range(len(density)):
        density[i].fill(vonmises(0,a_new[i],c_new[i],k_new[i]))
    for i in range(num_angles-1):
        for j in range(len(density)):
            temp[j].fill(vonmises(np.pi*2/num_angles*(i+1),a_new[j],c_new[j],k_new[j]))
        density = np.maximum(density,temp)
    r, th = np.meshgrid(rad, azm)
    ax2 = plt.subplot(projection="polar")
    ax2.set_theta_zero_location("W")
    ax2.set_theta_direction(1)
    ax2.set_yticklabels([])
    plt.pcolormesh(th, r, density,cmap='viridis',vmin=0,vmax=vmax)
    plt.colorbar(pad=0.1)
    plt.plot(azm, r, color='r', ls='none') 
    ax2.set_title("Measured relative imaging response",y=1.1)
    plt.savefig(savepath+"real_response.pdf",format="pdf",dpi=300)
    plt.show()  
    return(a_new)
def plot_fluorophore_density():
    global a_new
    global c_new
    global k_new
    fig = plt.figure()
    ax = Axes3D(fig)
    rad = np.linspace(0, 5, 500)
    azm = np.linspace(0, 2 * np.pi, 500)
    r, th = np.meshgrid(rad, azm)
    
    density = np.zeros((len(th),len(th[0])))
    for i in range(len(density)):
        density[i].fill(a_new[i])
    r, th = np.meshgrid(rad, azm)
    ax2 = plt.subplot(111,projection="polar")
    ax2.set_theta_zero_location("W")
    ax2.set_theta_direction(1)
    ax2.set_yticklabels([])
    plt.pcolormesh(th, r, density,cmap='viridis')
    plt.colorbar(pad=0.1)
    plt.plot(azm, r, color='r', ls='none') 
    ax2.set_title("Relative angular fluorophore distribution",y=1.1)
    savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace/figures/"
#    plt.savefig(savepath+"fluorophore_distribution.pdf",format="pdf",dpi=300)
    plt.show()  
def plot_optical_accessibility():
    global a_new
    global c_new
    global k_new
    k = k_new
    fig = plt.figure()
    ax = Axes3D(fig)
    rad = np.linspace(0, 5, 500)
    azm = np.linspace(0, 2 * np.pi, 500)
    r, th = np.meshgrid(rad, azm)
    
    density = np.zeros((len(th),len(th[0])))
    fwhm = (np.arccos(np.log(np.exp(np.abs(k))/2)/np.abs(k)))/np.pi*180*2
    for i in range(len(density)):
        density[i].fill(fwhm[i])
    r, th = np.meshgrid(rad, azm)
    ax1 = plt.subplot(111,projection="polar")
    ax1.set_theta_zero_location("W")
    ax1.set_theta_direction(1)
    ax1.set_yticklabels([])
    plt.pcolormesh(th, r, density,cmap='viridis')
    plt.colorbar(pad=0.1)
    ax1.plot(azm, r, color='r', ls='none') 
    ax1.set_title("Optical accessibility",y=1.1)
    savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace/figures/"
    plt.savefig(savepath+"optical_accessiblity.pdf",format="pdf",dpi=300)
    
    plt.show() 
get_real_data()
numangles=4
displaymin,displaymax=uniform_density(numangles)
#random_density(numangles,displaymin,displaymax)

plot_real_data(numangles,displaymin,displaymax)