#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Dec  6 12:07:05 2018

@author: henryhe
"""

import numpy as np
from lmfit import Model
import Utils as ut

class smart_rotation:
    def __init__(self,num_angles_in,angular_resolution_in):
        self.num_angles = num_angles_in
        self.angular_resolution = angular_resolution_in
        self.num_angles_evaluated = 360//self.angular_resolution
        self.countdata = np.zeros((self.num_angles,self.num_angles_evaluated))
        self.a = np.zeros((self.num_angles_evaluated,1))
        self.c = np.zeros((self.num_angles_evaluated,1))
        self.k = np.zeros((self.num_angles_evaluated,1))
        self.distribution = np.zeros((self.num_angles_evaluated,self.num_angles))
        self.coverage = np.zeros((self.num_angles_evaluated))
        
    def evaluate_angles(self,filepath, num_angles_in, angular_resolution_in,timepoint,usetimepoint=True):
    #    read data, evaluate and fit to model, generate distribution map
        for i in range(0, self.num_angles):
            if usetimepoint:
                countname = filepath+"angularcount"+str(timepoint).zfill(4)+"_"+str(i).zfill(4)+".txt"
            else:
                countname = filepath+"angularcount"+str(i).zfill(4)+".txt"
            with open(countname, "r") as countstream:
                for line in countstream:
                    currentline = line.split(",")
                    for j in range(0, len(currentline)):
                        self.countdata[i, j] = currentline[j]
        for i in range(0, self.num_angles_evaluated):
            r = self.countdata[:, i]
            gmodel = Model(ut.vonmises)
            x = np.array(range(0, 360, 360//self.num_angles))
            result = gmodel.fit(r, x=x, amp=r.max(0), cen=i *
                                self.angular_resolution, kappa=np.pi/4.0)
            self.a[i] = result.params['amp'].value
            self.c[i] = result.params['cen'].value
            self.k[i] = result.params['kappa'].value
            self.distribution[i] = ut.vonmises(
                np.arange(0, 360, 360//self.num_angles), self.a[i], self.c[i], self.k[i])
        return

    def estimate_coverage_average(self,angle_array):
        current_coverage = np.zeros(self.num_angles)
        for i in range(len(angle_array)):
            current_coverage = np.maximum(current_coverage,self.distribution[:,angle_array[i]])                
        return(np.mean(current_coverage/np.max(self.distribution,1)))
        def estimate_coverage_global(self,angle_array):
            current_coverage = np.zeros(self.num_angles)
            for i in range(len(angle_array)):
                current_coverage = np.maximum(current_coverage,self.distribution[:,angle_array[i]])                
            return(np.sum(current_coverage))
    def get_optimal_coverage(self,num_angles_needed):
    #    given the number of angles used, what combination gives the highest average percentage of maximum
        from itertools import combinations
        comb = list(combinations(range(24),num_angles_needed))
        coverage_percentage = 0
        for i in range(len(comb)):
            new_coverage_percentage = self.estimate_coverage_average(comb[i])
            if (new_coverage_percentage>coverage_percentage):
                winner = comb[i]
                coverage_percentage = new_coverage_percentage
        print(winner)
        return(winner)

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
