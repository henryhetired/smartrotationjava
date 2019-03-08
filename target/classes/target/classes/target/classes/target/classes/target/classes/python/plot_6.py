import sys
import matplotlib.pyplot as plt
import numpy as np
import math
from lmfit import Model
angles_used = set()
coverage = np.zeros(1)
a = np.zeros((2, 1))
c = np.zeros((2, 1))
k = np.zeros((2, 1))
fitted_distribution = np.zeros((2,1))
distribution = np.zeros((2, 1))
num_angles = 24
angular_resolution = 15
timepoint =31
workspace = "/mnt/fileserver/Henry-SPIM/smart_rotation/11222018/e5/data/workspace/"
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


def get_cmap(n, name='brg'):
    return plt.cm.get_cmap(name, n)


def evaluate_angles(filepath, num_angles_in, angular_resolution_in):
    global angular_resolution
    global num_angles
    global distribution    
    global coverage
    global a
    global c
    global k
    global timepoint
    num_angles = num_angles_in
    angular_resolution = angular_resolution_in
    num_angles_evaluated = 360//angular_resolution
    countdata = np.zeros((num_angles, num_angles_evaluated))
    for i in range(0, num_angles):
        countname = filepath+"angularcount"+str(timepoint).zfill(4)+"_"+str(i).zfill(4)+".txt"
        with open(countname, "r") as countstream:
            for line in countstream:
                currentline = line.split(",")
                for j in range(0, len(currentline)):
                    countdata[i, j] = currentline[j]
    print(countdata[0])
    a.resize(num_angles_evaluated, 1)
    c.resize(num_angles_evaluated, 1)
    k.resize(num_angles_evaluated, 1)
    distribution.resize(num_angles_evaluated, num_angles)
    coverage.resize(num_angles_evaluated)
    for i in range(0, num_angles_evaluated):
        r = countdata[:, i]
        gmodel = Model(vonmises)
        x = np.array(range(0, 360, 360//num_angles))
        result = gmodel.fit(r, x=x, amp=r.max(0), cen=i *
                            angular_resolution, kappa=np.pi/4.0)
        a[i] = result.params['amp'].value
        c[i] = result.params['cen'].value
        k[i] = result.params['kappa'].value
        distribution[i] = vonmises(
            np.arange(0, 360, 360//num_angles), a[i], c[i], k[i])
#    a = a.flatten()/(np.pi*2*np.i0(k))
#    print(a.flatten())
#    print(c.flatten())
#    print(k.flatten())
    return
        
def find_next_global():
    #given a mip of a couple of views, what's the next best view based on the overall number of foreground blocks
    global num_angles
    global distribution
    global coverage
    global angles_used
    angles_to_try = set(range(num_angles))
    angles_to_try = angles_to_try.difference(angles_used)
    coverage_sum = np.sum(coverage,0)
    for i in range(len(angles_to_try)):
        try_angle = angles_to_try.pop()
        new_coverage = np.maximum(coverage,distribution[:,try_angle])
        new_coverage_sum = np.sum(new_coverage)
        if (new_coverage_sum>coverage_sum):
            coverage_sum = new_coverage_sum
            current_winner = try_angle
    coverage = np.maximum(coverage,distribution[:,current_winner])
    angles_used.add(current_winner)
    return;


def estimate_coverage_average(angle_array):
    current_coverage = coverage
    for i in range(len(angle_array)):
        current_coverage = np.maximum(current_coverage,distribution[:,angle_array[i]])                
    return(np.mean(current_coverage/np.max(distribution,1)))
def estimate_coverage_global(angle_array):
    current_coverage = coverage
    for i in range(len(angle_array)):
        current_coverage = np.maximum(current_coverage,distribution[:,angle_array[i]])                
    return(np.sum(current_coverage))
def get_optimal_coverage(num_angles):
#    given the number of angles used, what combination gives the highest average percentage of maximum
    global distribution
    from itertools import combinations
    comb = list(combinations(range(24),num_angles))
    coverage_percentage = 0
    for i in range(len(comb)):
        new_coverage_percentage = estimate_coverage_average(comb[i])
        if (new_coverage_percentage>coverage_percentage):
            winner = comb[i]
            coverage_percentage = new_coverage_percentage
    print(winner)
def get_optimal_global(num_angles): 
    from itertools import combinations
    comb = list(combinations(range(24),num_angles))
    global distribution
    global coverage
    coverage_sum = np.sum(coverage,0)
    variation = np.zeros(len(comb))
    for i in range(len(comb)):
        new_coverage_sum = estimate_coverage_global(comb[i])
        variation[i] = new_coverage_sum
        if (new_coverage_sum>coverage_sum):
            winner = comb[i]
            coverage_sum = new_coverage_sum
    print(np.var(variation/np.linalg.norm(variation)))
    print(winner)

def run_global(num_angles):   
#    perform optimization based on overall number of foreground blocks
    global coverage
    global a
    global angles_used
    global workspace
    global angular_resolution
    global timepoint
    angles_used.clear()
    evaluate_angles(
            workspace, 24, angular_resolution)    
    first_angle = 0
    angles_used.add(first_angle)
    coverage = distribution[:,first_angle]
    plt.close()
    plt.hold(True)
    plt.xlabel("Angle within sample")
    plt.ylabel("Number of foreground blocks")
    plt.xlim((0,360))
    plt.ylim((0,20000))
    plt.plot(range(0,360,angular_resolution),np.max(distribution,1),'--', label = "Maximum")
    plt.plot(range(0,360,angular_resolution),coverage,label="View %02d"%0)
    name = workspace+"/figures/t"+str(timepoint).zfill(4)+"coverage_%02d.pdf"
    ax = plt.subplot(111)
    ax.legend(bbox_to_anchor=(1.2,1),labelspacing = 0.01,fontsize = 5,frameon=False)
    plt.savefig(name%0,dpi = 500,format = "pdf",bbox_inches="tight")
    for i in range(1,num_angles-1):
        find_next_global()
        plt.plot(range(0,360,angular_resolution),coverage,label="View %02d"%i)
        ax.legend(bbox_to_anchor=(1.2,1),labelspacing = 0.01,fontsize = 5,frameon=False)
        plt.title("Estimated coverage after fusion")
        plt.savefig(name%i,dpi = 500,format = "pdf",bbox_inches="tight")

#evaluate_angles(workspace, 24, 15)
num_angles = 4
#get_optimal_coverage(num_angles)
#get_optimal_global(num_angles)
run_global(num_angles)