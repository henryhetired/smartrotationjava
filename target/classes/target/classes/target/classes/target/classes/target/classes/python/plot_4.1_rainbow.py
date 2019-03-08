#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Sep  4 16:46:30 2018

@author: henryhe
"""
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.mlab as mlab
import matplotlib.patches as mpatches
savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_test/figures/"
savename = "information_content_full2"     
name = savepath+savename+".pdf"                   
def get_cmap(n, name='brg'):
    '''Returns a function that maps each index in 0, 1, ..., n-1 to a distinct 
    RGB color; the keyword argument name must be a standard mpl colormap name.'''
    return plt.cm.get_cmap(name, n)
    
cmap = get_cmap(24,'hsv');
for idx in range(0,24):
    ax = plt.subplot(111,polar=True)
    ax.set_theta_zero_location("W")
    ax.set_theta_direction(1)
    ax.set_rmax(10000)
#    ax.set_yticks(np.linspace(0,10000,4,endpoint=False))
    ax.set_yticks([])
    ax.grid(False)
    ax.bar(np.pi/12*idx,10000,width=2*np.pi/24,color=cmap(idx),alpha=0.3)
    ax.hold(True)
    
    ax.set_rmax(10000)
#ax.legend(handles = patches,bbox_to_anchor=(1.2,1),labelspacing = 0.01,fontsize = 5,frameon=False)
#plt.savefig(name,dpi = 500,format = "pdf",bbox_inches="tight")
