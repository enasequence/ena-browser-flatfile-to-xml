# ena-browser-flatfile-to-xml
Convenience tool for ENA Browser users to convert EMBL flatfile format to XML

## Purpose

The ENA Browser will be phasing out the option to download sequence records in XML format with the upcoming release of the
new ENA Browser. The current ENA Browser already does not provide an XML option for WGS/TSA sequences.
 
This tool is made available to ease the transition for any users of the XML format. Please note that support for converting
EMBL flatfile format to XML is no longer maintained in ENA, and thus this tool is provided as is. Future changes to EMBL flatfile 
format and/or validation rules will not be reflected in the XML format.
We recommend that any pipelines that depend on the XML format be transitioned to use the flatfile format. The Sequence Tools libraries
will prove useful in processing EMBL flatfile format.
https://github.com/enasequence/sequencetools

## Note: 
This tool converts only one record at a time. If your input file contains multiple records, only the first one will be converted to XML.

## Usage
This tool requires Java runtime 8 or higher to be available. 

1. Download and extract the latest release 
2. Invoke the converter using the scripts in extracted folder

#### Arguments:
1. full path to flatfile format file
2. full path to output xml file
3. [Optional] flatfile format EMBL/CDS/NCR/MASTER

### Windows
..\ena-browser-flatfile-to-xml-1.0.0>ff-to-xml.cmd <full-path-to-flat-file> <full-path-to-output-xml-file>

### Linux/Mac
../ena-browser-flatfile-to-xml-1.0.0$ ./ff-to-xml.sh <full-path-to-flat-file> <full-path-to-output-xml-file>
