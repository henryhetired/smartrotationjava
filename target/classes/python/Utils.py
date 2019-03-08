#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Mar  1 14:27:50 2019

@author: henryhe
"""
try:    
    import numpy as np
    import matplotlib.pyplot as plt
    import math
except ImportError:
    import pip
    pip.main(['install','--user','numpy'])
    pip.main(['install','--user','numpy'])
def get_cmap(n, name='brg'):
    return plt.cm.get_cmap(name, n)
def vonmises(x, amp, cen, kappa):
    # "1-d vonmises"
    top = (amp/(np.pi*2*np.i0(kappa)))
    bot = np.exp(kappa*np.cos(x/360.0*2.0*np.pi-cen))
    return top*bot
def inv_vonmises(y, amp, cen, kappa):
    first = np.log(y*np.pi*2*np.i0(kappa)/amp)/kappa
    second = math.acos(first)
    result = np.zeros(2)
    result[0] = second+cen
    result[1] = 2*np.pi-second+cen
    return result
class sr_configuration:
    def __init__(self,pathtoconfig):
        with open(pathtoconfig) as f:
            content = f.readlines()
        content = [x.strip() for x in content]
        for i in content:
            if "nAngles" in i:
                self.nangles = int(i[i.find('=')+1:])
            if "angular" in i:
                self.angularresolution = int(i[i.find('=')+1:])
            