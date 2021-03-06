#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Dec  6 12:07:05 2018

@author: henryhe
"""

import sys
import Utils as ut
from smart_rotation import smart_rotation
import matplotlib.pyplot as plt

config = ut.sr_configuration(sys.argv[1]+"config.txt")
sr = smart_rotation(int(360/config.angularresolution),config.angularresolution,config.evaluationresolution)
if len(sys.argv)==3:
    sr.evaluate_angles(sys.argv[1],timepoint = 0,usetimepoint=False)
else:
    sr.evaluate_angles(sys.argv[1],sys.argv[2],usetimepoint=True)

angles_get = sr.get_optimal_coverage(config.nangles)
if sys.argv[3]=="1":
    coverage_curve = sr.gen_figure()
    fig = plt.figure(figsize=(5,5))
    ax = plt.subplot(111)
    ax.plot(np.arange(1,7),coverage_curve)
    ax.set_title("Coverage vs num_angles")
    ax.set_xlabel("Number of angle used")
    ax.set_ylabel("Coverage percentage")
    plt.savefig(sys.argv[1]+"CoverageVsNumAngle.jpg",format="jpg")
print(','.join(map(str,angles_get)))
#pathtotext = "/mnt/fileserver/Henry-SPIM/smart_rotation/11222018/e5/data/workspace/"
#result = np.zeros((32,4))
#coverage_optimal = np.zeros(32)
#coverage_standard = np.zeros(32)
#for i in range(32):
#    timepoint = i
#    sr = smart_rotation(24,15)
#    sr
#    sr.evaluate_angles(pathtotext, 24, 15,timepoint)
#    num_angle = 4
#    angles_get = sr.get_optimal_coverage(num_angle)
#    result[i] = angles_get
#    coverage_optimal[i] = sr.estimate_coverage_average(angles_get)
#    coverage_standard[i] = sr.estimate_coverage_average(np.arange(0,sr.num_angles,6))
