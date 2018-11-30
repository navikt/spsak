#!/bin/bash
########################################################
# Shell script for stoping av Nav Batcher med Rest API #
# Parameter 1: batch navn                              #
# Parameter 2: Rest Resource (optional)                #
########################################################

#VARIABLES
restUrl="localhost:8080/fpsak/rest/batch"
batchName=""

#FUNCTIONS

#validate parameters
function validateParams
{

	#exit if param is missing
	if [ "$batchName" = "" ]; then
		printUsage
		exit 1
	fi

}
#print usage
function printUsage
{
	echo "Usage: sudo ./stopBatch.sh -b batchName [-r restURL]"
	echo "ex 1:  sudo ./stopBatch.sh -b BATCH001"
	echo "ex 2:  sudo ./stopBatch.sh -b BATCH001 -r \"localhost/project/rest/batch\""
}


#MAIN LOGIC
# read input parameters
while [ "$1" != "" ]; do
	case $1 in

		-r | --resource ) shift
						restUrl=$1
						;;
        -b | --batch ) shift
						batchName=$1
						;;
        -h | --help )  printUsage
						exit 0
						;;
	esac
	shift
done

#validate parameters
validateParams

# stop batch (curl)
response=$(curl $restUrl/stop/$batchName 2>/dev/null)
if [ "$response" = "true" ]; then
	exit 0
else
	echo "$response"
	exit 16
fi
