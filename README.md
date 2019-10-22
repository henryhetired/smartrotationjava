Smart Rotation workflow software                                                                                         
===============================                                                                                          
Smart Rotation workflow software is used to intelligently analyse a multi-view dataset and select the best view combination that gives the optimal imaging response.                                                                             
                                                                                                                         
                                                                                                                         
Requirements:                                                                                                            
------------                                                                                                             
* 64-bit operating System (Linux or Windows)                                                                             
* `CUDA` 9.0.176 driver installed on an analysis computer            
    * The version of `CUDA` driver required is determined by the `jcuda` [jcuda.org] version being used.                                                                  
* `CUDA` enabled graphics card with at least **4GB** of VRAM
    * VRAM size dictates the size of Z-STACK that can be analysed.                                                            
* A TCP/IP connection from the microscope to the analysis computer                                                       
                                                                                                                         
Tested hardware:                                                                                                         
----------------                                                                                                         
Graphics card | Operating System                                                                                         
--------------|-----------------                                                                                         
NVIDIA Quadro M5000 | Windows 10                                                                                         
NVIDIA Quadro M5000 | Windows Server 2016                                                                                
NVIDIA Titan | Ubuntu 16.4                                                                                               
NVIDIA Quadro P5000 | Centos 7                                                                                           
                                                                                                                         
                                                                                                                         
Installation:                                                                                                            
-------------                                                                                                            
* Download the content of the repository to the machine that is running the analysis server.                              
* Follow the instruction from the instruction.txt to configure the server and client.

Demo:
-------------
* Download the demo data file from [https://uwmadison.box.com/s/lswqqiok2t883kth5me8195ybxk43872]
* The demo data includes a 24-view dataset of a zebrafish embryo with histone nucleus labelling
* Start the analysis server by running `run "java -jar SmartRotationProcessing.jar $server_config". $server_config is the location of the server configuration file. See server_config.txt for sample usage.
* Run the full 24-angle evaluation on the dataset:
  1. Initialize the analysis server by sending `runcommand initialize $LOCATION_OF_WORKSPACE` to the analysis machine
  2. Run the full evaluation by sending `runcommand evaluation $LOCATION_OF_TESTDATA 0 1`
  3. The 4 angles that are optimal for image coverage can be queryed via `runcommand getupdate`

The full evaluation should take around 2 mins depends on the speed of the filestorage.
                                                                                                                         
                                                                                                                         
                                                                                                                         
                                                                                                                         
                                                           
