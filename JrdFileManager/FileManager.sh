 #!/bin/bash
#./FileManager.sh.sh 'ODM' 'v1.2.3.4.3432.0'
if [ $1 == 'ODM' ]; then
     echo "Prepare for ODM apk."
     sed -i "s/applicationId *\".*\"/applicationId \"com.jrdcom.filemanager.odm\"/" ./app/build.gradle
elif [ $1 == 'Global' ]; then
     echo "Prepare for Global apk."
     sed -i "s/applicationId *\".*\"/applicationId \"com.jrdcom.filemanager\"/" ./app/build.gradle
else
     echo "No update."
fi
