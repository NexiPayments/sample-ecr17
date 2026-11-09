VERSION 2.6

The project was created to enable developers of applications using the ECR 17 
protocol to access a series of examples illustrating the implementation methods 
specified by the protocol.

The project consists of the following classes implemented in Java :

# ConstantSample.java
# MainSampleECR01.java
# LaunchECRCommandSample.java
# ECRBuildFramesSample.java
# ECRLogFramesSample.java
# ECRServiceSample.java
# ECRTicketParserSample.java
# UtilsSample.java

Brief description of the functionality of the individual classes.

# ConstantSample.java
    It contains the fixed parameters used in the project.

# MainSampleECR01.java
    Main project class.
    Allows launching the examples for each ECR 17 command and 
    receiving the results from the POS.

# LaunchECRCommandSample.java
    The methods of this class are called in the project's Main Class.
    Allows to execute ECR 17 commands with preset parameters.

# ECRBuildFramesSample.java
    Each ECR 17 command sent to the pos, must be included in a byte array (frame) 
    correctly formatted according to the specifications.
    The class includes methods for constructing the frames in accordance 
    with the specifications.

# LogEcrToPosFramesSample.java
    ECR request commands consist of an array of bytes (frame) to be sent to the pos.
    The class contains methods that extract data from the frames, 
    which are subsequently displayed.

# LogPosToEcrFramesSample.java
    The POS response messages consist of one or more byte arrays (frames).
    The class contains methods that extract data from the frames, 
    which are subsequently displayed.

# ECRServiceSample.java
    The class includes the implementation of each ECR 17 command 
    and the methods for sending data to and receiving data from the POS.

# ECRTicketParserSample.java
    Support class containing methods for extracting the receipt sent by the POS.

# UtilsSample.java
    The class includes a series of support methods necessary for the correct 
    implementation of the ECR 17 protocol.