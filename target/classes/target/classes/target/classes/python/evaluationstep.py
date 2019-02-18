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
    num_angles = num_angles_in
    angular_resolution = angular_resolution_in
    num_angles_evaluated = 360//angular_resolution
    countdata = np.zeros((num_angles, num_angles_evaluated))
    for i in range(0, num_angles):
        countname = filepath+"angularcount"+str(i).zfill(4)+".txt"
        with open(countname, "r") as countstream:
            for line in countstream:
                currentline = line.split(",")
                for j in range(0, len(currentline)):
                    countdata[i, j] = currentline[j]
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
    coverage_sum = np.sum(coverage,0)
    for i in range(len(comb)):
        current_coverage = coverage
        new_coverage_percentage = estimate_coverage_average(comb[i])
        if (new_coverage_percentage>coverage_percentage):
            winner = comb[i]
            coverage_percentage = new_coverage_percentage
    print(winner)
    return(winner)
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
    print(winner)
    return(winner)
def getrequirednumberofangles(percentage):
    # get the number of angles needed to get the percentage cover
    global angular_resolution
    global num_angles
    global distribution
    global a
    global c
    global k
    angles = np.array([])
    anglestried = np.array([])
    coverage = np.zeros(360//angular_resolution)
    # The most information rich angle, start from here
    maxinfoidx = int(
        np.floor(np.argmax(a)*(360/angular_resolution/num_angles)))
    currentangle = maxinfoidx
    np.appen(anglestried, currentangle)
    np.append(angles, currentangle)
    print(currentangle)
#    while (np.sum(coverage) != 360//angular_resolution):
    for j in range(2):
        for i in range(360//angular_resolution):
            tempmax = a[i]/(np.pi*2*np.i0(k[i])) / \
                np.exp(np.minimum(-k[i], k[i]))
            print(tempmax)
            print(a[i])
            if (distribution[i][currentangle] >= tempmax*percentage):
                coverage[i] = 1
        currentangle = int((np.argmax(coverage > 0)-1)*10/15)
        print(currentangle)
        print(coverage)
        np.append(angles, currentangle)

    return angles

def run_global(filepath,num_angles):   
#    perform optimization based on overall number of foreground blocks
    global coverage
    global a
    global angles_used
    evaluate_angles(
            filepath, 24, 10)    
    first_angle = 0
    angles_used.add(first_angle)
    coverage = distribution[:,first_angle]
    plt.hold(True)
    plt.xlabel("Angle within sample")
    plt.ylabel("Number of foreground blocks")
    plt.xlim((0,360))
    plt.ylim((0,14000))
    plt.plot(range(0,360,10),np.max(distribution,1),'--', label = "Maximum")
    plt.plot(range(0,360,10),coverage,label="View %02d"%0)
    name = filepath+"figures/coverage_%02d.pdf"%0
    ax = plt.subplot(111)
    ax.legend(bbox_to_anchor=(1.2,1),labelspacing = 0.01,fontsize = 5,frameon=False)
#    plt.savefig(name,dpi = 500,format = "pdf",bbox_inches="tight")
    for i in range(1,num_angles-1):
        find_next_global()
        plt.plot(range(0,360,10),coverage,label="View %02d"%i)
        name = filepath+"figures/coverage_%02d.pdf"%i
        ax.legend(bbox_to_anchor=(1.2,1),labelspacing = 0.01,fontsize = 5,frameon=False)
        plt.title("Estimated coverage after fusion")
#        plt.savefig(name,dpi = 500,format = "pdf",bbox_inches="tight")
        
#run_global(4)
#print(angles_used)
pathtotext = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_final3/angularcount_final/"
savepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace_final3/figures/"
coverage_optimal = np.zeros(32)
coverage_standard = np.zeros(32)
evaluate_angles(pathtotext, 24, 10)
blind = np.zeros(10)
smart = np.zeros(10)
for i in np.arange(1,10):
    num_angle = i
    print(i)
    smart_angles = get_optimal_global(num_angle)
    blind_angles = tuple(np.arange(0,24,24/num_angle).astype('int'))
    blind[i-1] = estimate_coverage_average(blind_angles)
    smart[i-1] = estimate_coverage_average(smart_angles)
#run_global(num_angles)
f, (ax,ax2) = plt.subplots(2,1,sharex=True,gridspec_kw = {'height_ratios':[15, 1]},figsize = (3.4,2.55))
plt.subplots_adjust(left=0,right=1,bottom=0,top=1)
plt.hold(True)
ms = 3
ax.plot(np.arange(1,10),blind[:-1],'r-o',label='Blind multi-view',alpha=0.5,markersize=ms)
ax.plot(np.arange(1,10),smart[:-1],'g-x',label='Smart rotation',alpha=0.5,markersize=ms)
ax2.plot(np.arange(1,10),blind[:-1],'r-o',label='Blind multi-view',alpha=0.5,markersize=ms)
ax2.plot(np.arange(1,10),smart[:-1],'g-x',label='Smart rotation',alpha=0.5,markersize=ms)
ax2.set_ylim([0,0.01])
ax.set_ylim(0.4,1)
ax.spines['bottom'].set_visible(False)
ax2.spines['top'].set_visible(False)
ax.xaxis.tick_top()
ax2.tick_params(labeltop='off')
ax2.xaxis.tick_bottom()
d = .015
kwargs = dict(transform=ax.transAxes, color='k', clip_on=False)
ax.plot((-d, +d), (-d, +d), **kwargs)        # top-left diagonal
ax.plot((1 - d, 1 + d), (-d, +d), **kwargs)  # top-right diagonal

kwargs.update(transform=ax2.transAxes)  # switch to the bottom axes
ax2.plot((-d, +d), (1 - d, 1 + d), **kwargs)  # bottom-left diagonal
ax2.plot((1 - d, 1 + d), (1 - d, 1 + d), **kwargs)  # bottom-right diagonal
ax.legend(loc='lower right')
ax2.set_xlabel('Number of views used',fontsize=15)
ax.set_ylabel('Coverage percentage',fontsize=15)
ax.set_title('Sample coverage vs Number of views',fontsize=15)
plt.savefig(savepath+"convergence_comparison.pdf",dpi=500,format="pdf")