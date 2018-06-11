# GTNav - Georgia Tech Campus Navigation

Android application to help students navigate the Georgia Tech campus for walking, riding buses, and biking.

## Releases

The latest release download and release notes can be found in the releases section [here](https://github.com/spybug/GTCampusNavigation/releases).


## Install Guide

### Android Application

#### Pre-requisites:

- Android phone with API 21 or greater (Android 5.0)

#### Dependent libraries:

- Android Studio ≥ 3.0 which includes:
  - Java JDK ≥ 7
  - Android SDK
  - Android BuildTools
  - Gradle ≥ 3.1.1

#### Download instructions:

Download the apk file from the github release here: https://github.com/spybug/GTCampusNavigation/releases

The source code can also be accessed from github as well: https://github.com/spybug/GTCampusNavigation/tree/master/Android

#### Build instructions for source code:

When trying to run the source code, open the Android folder in Android Studio and run a Gradle build on the project.

#### Installation of actual application:

To install the APK file, download the file to your phone. Make sure in your system settings that installation from unknown sources is allowed. Then open the APK file on your phone and install it.

#### Run instructions:​

To run the application, open the app icon on your phone. To run the source code press the run button in Android studio while your phone is connected via USB or while an emulator is running.

#### Troubleshooting:

If you have errors while building, run “Rebuild Project” which will clean out the project and attempt to rebuild it.


### Server

#### Pre-requisites:​ 
Must have a computer that can run python version 3.4.

#### Dependent libraries:
Must download and configure python 3.4 with pip.

Dependent python libraries include:
- Flask ≥ (v0.12.2)
- polyline ≥ (v1.3.2)
- requests ≥ (v2.18.4)
- xmltodict ≥ (v0.11.0)
- pyodbc ≥ (v3.0.10)

#### Download instructions:​ 
Download the server files from: https://github.com/spybug/GTCampusNavigation/tree/master/Server

#### Build instructions: 
To download the dependent libraries use the command line to set your active directory to the server folder and type `pip install -r requirements.txt`

#### Installation of actual application: ​
No installation required

#### Run instructions:​ 
To run the server, simply run `python main.py` in the Server folder.

#### Troubleshooting: 

- Can’t find certain import error:
  - Make you meet the version requirements for each of the dependent libraries and are running the correct Python version.
- When running the file and you get “Error: could not import config.ini”:
  - Please make sure to create the config.ini file based on the example or download from google drive. This contains the sensitive login info for the database.
- If you type in the request wrong to the url you get “Not Found, the requested url was not found on the server”:
  - Make sure you spelled the url correctly (you can verify these urls in the `main.py` file)