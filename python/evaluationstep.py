import sys
import matplotlib.pyplot as plt
import numpy as np
import math
from lmfit import Model

a = np.zeros((2, 1))
c = np.zeros((2, 1))
k = np.zeros((2, 1))
distribution = np.zeros((2, 1))
num_angles = 0
angular_resolution = 0


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
    print(a.flatten())
    print(c.flatten())
    print(k.flatten())
    return


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


evaluate_angles(
    "/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace/angularcount/", 24, 10)
# evaluate_angles(sys.argv[1], int(sys.argv[2]), int(sys.argv[3]))
print(getrequirednumberofangles(0.8))
