Instruction to configure the smart rotation server and client

config file need to be in the workspace

Make sure the port that the workflow need to work on is open

To start the program:


from command line:


run "java -jar SmartRotationProcessing.jar $port_number"


After "Server started" printed, the server is ready to accept processing request using the following command structure:


----Command structure----
0) Initialize the analysis server
   initialize $workspace
   EXAMPLE COMMAND:

   initialize /mnt/fileserver/workspace/

1) Evaluate the fix spacing single timepoint data:
   evaluation $filepath $timepoint $numberofangles $gap
   EXAMPLE COMMAND:

   evaluation /mnt/fileserver/data/ 0 2

   evaluate data stored at /mnt/fileserver/data/ timepoint 0 with 2 gap (if 24 angles used, 12 angles evaluated)
2) Process a single angular stack that comes in
   processangle $filepath $timepoint $angle
   EXAMPLE COMMAND:

   evaluation /mnt/fileserver/data/ 0 5

   evaluate data at /mnt/fileserver/data/, specifically data at timepoint 0, angleidx 5
3) Send the current estimation of optimal angle back to the client
   angles=$a0,$a1.....

