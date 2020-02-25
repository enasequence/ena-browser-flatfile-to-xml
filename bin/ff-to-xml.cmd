@echo off
set JAR=.\ena-browser-flatfile-to-xml.jar
set JAVA=java -Xms128m -Xmx4096m
IF "%~1"=="" GOTO noargs
IF "%~2"=="" GOTO noargs
IF "%~3"=="" GOTO defaultformat

%JAVA% -jar %JAR% --flatfile="%1" --xmlfile="%2" --format="%3"
if errorlevel 1 (
   echo Exited with error code %errorlevel%
)
GOTO :end

:noargs
echo Usage: "<flatfile path> <xml output path> <flatfile format: EMBL/CDS/NCR/MASTER>"
echo Last argument is optional. Default format is EMBL.
echo e.g.
echo ff-to-xml.cmd c:\user\ABC.txt c:\user\ABC.xml CDS
GOTO :end

:defaultformat
%JAVA% -jar %JAR% --flatfile="%1" --xmlfile="%2"
if errorlevel 1 (
   echo Exited with error code %errorlevel%
)

:end


