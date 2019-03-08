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
import matplotlib.patches as mpatches

def gaussian(x, amp, cen, wid):
    "1-d gaussian: gaussian(x, amp, cen, wid)"
    return (amp/(np.sqrt(2*np.pi)*wid)) * np.exp(-(x-cen)**2 /(2*wid**2))
window_size = 5

countdata = np.zeros((24,36))

avgdata = np.zeros((24,36))

filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_final3/"
for i in range(0,24):
    countname = filepath+"/angularcount_final/angularcount"+str(i).zfill(4)+".txt"
    with open(countname,"r") as countstream:
        for line in countstream:
            currentline = line.split(",")
            for j in range(0,len(currentline)):
                countdata[i,j] = currentline[j]
#savepath = "Z:\\Henry-SPIM\\11132017\\e2\\t0000\\analysis\\analysis5 angular_plot\\figures\\plot 3_1\\"
savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_final3/figures/"
savename = "information_content_full"     
name = savepath+savename+".pdf"                   
def get_cmap(n, name='brg'):
    '''Returns a function that maps each index in 0, 1, ..., n-1 to a distinct 
    RGB color; the keyword argument name must be a standard mpl colormap name.'''
    return plt.cm.get_cmap(name, n)
    
cmap = get_cmap(24,'hsv');
patches = []
name = savepath+savename+".pdf"
fig = plt.figure(figsize=(1.8,1.8))    
plt.subplots_adjust(left=0,right=1,bottom=0,top=1)  
for idx in range(0,24):
    normalized = countdata[idx,:]
    maxidx = np.argmax(countdata[idx,:])
    x = range(0,360,10)
    x = np.array(x)
    ax = plt.subplot(111,polar=True)
    ax.set_theta_zero_location("W")
    ax.set_theta_direction(1)
    ax.set_rmax(14000)
    ax.set_yticks(np.linspace(0,10000,4,endpoint=False))
    theta = np.linspace(0.0,2*np.pi,36,endpoint=False)
    ax.bar(-theta,normalized,width=2*np.pi/36,color=cmap(idx),alpha = 0.2)
    ax.tick_params(axis='y',labelsize=0,labelcolor='w')
    plt.xticks(visible=False)
    ax.hold(True)  
    ax.set_rmax(14000)
plt.savefig(name,dpi = 500,format = "pdf")
plt.show()