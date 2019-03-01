#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Mar  1 13:34:27 2019

@author: henryhe
"""

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Feb 14 11:57:06 2019

@author: henryhe
"""
#Script to evaluation the response of each angle relative to the imaging angle
import matplotlib.pyplot as plt
import numpy as np
from evaluationstep_final import smart_rotation
filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_final3/angularcount_final/"
sr = smart_rotation(24,10)
sr.evaluate_angles(filepath,24,15,0,False)
savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_final3/figures/"
alpha = 0.2
fig = plt.figure(figsize=(4.5,3))
plt.subplots_adjust(left=0,right=1,top=1,bottom=0)

plt.plot(np.arange(0,360,10),np.divide(sr.a[:,0]/np.pi/2,np.i0(sr.k))*np.exp(np.abs(sr.k))[:,0],'--',label="fit")
for i in np.arange(45,360,90):
    plt.axvline(x=i,linestyle='--',c='g',alpha=alpha)
plt.grid(False)
plt.xlim(0,360)
plt.ylim(0,15000)
plt.xlabel('imaging angle')
plt.ylabel('Average Number of foreground block')
plt.show()
#plt.savefig(savepath+"Optical image coverage blind"+".pdf",dpi = 500,format = "pdf") 