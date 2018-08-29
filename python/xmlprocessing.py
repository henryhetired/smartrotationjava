# -*- coding: utf-8 -*-
"""
Created on Sat Nov 11 14:58:14 2017

@author: jhe1
"""

import xml.etree.cElementTree as ET
for i in range(0,24):
    angle = str(15*i)
    filepath = "/mnt/fileserver//Henry-SPIM//smart_rotation//06142018//sample1//c00//";
    filebase = "t0000_conf%04d_view0000_c00.xml" % i;
    root = ET.Element("root");
    stage_pos = ET.SubElement(root,"stage_pos");
    ET.SubElement(stage_pos,"x").text = "5"
    ET.SubElement(stage_pos,"y").text = "6"
    ET.SubElement(stage_pos,"start_z").text = "7"
    ET.SubElement(stage_pos,"end_z").text = "8"
    ET.SubElement(stage_pos,"angle").text = angle
    image_attribute = ET.SubElement(root,"image_attributes")
    ET.SubElement(image_attribute,"xypixelsize").text = "0.65"
    ET.SubElement(image_attribute,"zpixelsize").text = "2"
    ET.SubElement(image_attribute,"width").text = "2048"
    ET.SubElement(image_attribute,"height").text = "2048"
    ET.SubElement(image_attribute,"nImages").text = "422"
    ET.SubElement(image_attribute,"bit_depth").text = "16"
    ET.SubElement(image_attribute,"gapbetweenimages").text = "0"
    ET.SubElement(image_attribute,"background").text = "700"
    sample_position = ET.SubElement(root,"sample_position")
    ET.SubElement(sample_position,"startX").text = "0"
    ET.SubElement(sample_position,"endX").text = "2048"
    ET.SubElement(sample_position,"startZ").text = "0"
    ET.SubElement(sample_position,"endZ").text = "106"
    smart_rot_params= ET.SubElement(root,"smart_rot_param")
    ET.SubElement(smart_rot_params,"blk_size").text = "16"
    ET.SubElement(smart_rot_params,"entropybackground").text = "7.4"
    ET.SubElement(smart_rot_params,"angular_resolution").text = "10"
    tree = ET.ElementTree(root)
    tree.write(filepath+filebase)