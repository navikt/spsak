#!/bin/bash
#########################################################
# Shell script for start av Nav Batcher med Rest API    #
# Parameter 1: batch navn                               #
# Parameter 2: batch Parameter                          #
# Parameter 3: Rest Resource (optional)                 #
# Parameter 4: sleep between polls (optional)           #
# Parameter 5: serverlog (optional)                     #
#########################################################

#VARIABLES
pollSleep="5s"
batchName=""
batchParams=""
serverLog="/var/log/apps/fpsak/server/server.log"
propertiesFil="/app/fpsak/configuration/environment.properties"
maxPeriode=30 # dagerTilJul=$((($(date -u --date="2017-12-24 18:00:00" +%s) - $(date -u --date="now" +%s))/(60*60*24)))

# Required for login
source isso-login.sh
systembrukerUsername=$(propertyValue "systembruker.username")
systembrukerPassword=$(propertyValue "systembruker.password")
issohostUrl="$(propertyValue "OpenIdConnect.issoHost" | sed  's/\\:/:/g' | sed 's/oauth2//')"
loadbalancerUrl="$(extractHostFromLoadbalancerUrl $(propertyValue "loadbalancer.url" | sed  's/\\:/:/g'))"
defaultPort="8443"
restUrl="https://${loadbalancerUrl}:${defaultPort}/fpsak/api/batch"

#FUNCTIONS
function validateParams
{
	if [ "$batchName" = "" ]; then
		printUsage
		exit 1
	fi
}

#print usage
function printUsage
{
	echo "Usage: sudo ./startBatch.sh -b batchName [-p batchParameters] [-r restURL] [-s sleepTime] [-l serverLog] [-n periode]"
	echo "ex 2:  sudo ./startBatch.sh -b BATCH001 -p \"fom=21-11-2016, tom=23-11-2016\" -r \"localhost/project/rest/batch\" -s 10s"
}

function cleanUpAndExit
{
    if [ -f ${curlCookieJar} ]; then
        issoLogout ${issohostUrl} ${curlCookieJar}
    fi
    exit ${1}
}

addListener EXIT cleanUpAndExit
addListener SIGTERM cleanUpAndExit
addListener SIGINT cleanUpAndExit

#MAIN LOGIC
# read input parameters
while [ "$1" != "" ]; do
	case $1 in

		-r | --resource ) shift
						restUrl=$1
						;;
		-s | --sleepInterval ) shift
						pollSleep=$1
						;;
         -b | --batch ) shift
						batchName=$1
						;;
         -p | --params ) shift
						batchParams=$1
						;;
		-l  | --log  )  shift
						serverLog=$1
						;;
         -h | --help )  printUsage
						exit 0
						;;
	esac
	shift
done

#validate parameters
validateParams
curlCookieJar=/tmp/${batchName}-cookiejar-${BASHPID}
loadbalancerUrlToLocalhost="--resolve '${loadbalancerUrl}:${defaultPort}:127.0.0.1' --insecure"

# LOGIN
issoLogin ${systembrukerUsername} ${systembrukerPassword} ${issohostUrl} ${curlCookieJar}

# Gjør request til batch/init vil logge "batchbrukeren" inn i VL
curl --silent ${loadbalancerUrlToLocalhost} --cookie ${curlCookieJar} --cookie-jar ${curlCookieJar} --location --request GET "${restUrl}/init"
# START BATCH
# create JsonObject with  batch params
jsonObject="{\"jobParameters\":\"$batchParams\"}"

# call cURL command with Json object and params, store execution ID
requestCmd=$(curl --cookie ${curlCookieJar} --cookie-jar ${curlCookieJar} ${loadbalancerUrlToLocalhost} --header "Content-Type: application/json" --write-out "\nHTTP %{http_code}" --request POST -d "$jsonObject"  ${restUrl}/launch?batchName=${batchName} 2>/dev/null)
http_status=$(echo "$requestCmd" | grep HTTP |  awk '{print $2}')

#WAIT FOR BATCH
## poll status (curl)
echo
if [[ "$http_status" -eq "200" ]]; then
    # ExecutionId starter med batchnavnet
    executionId=$(echo "$requestCmd" | grep ${batchName})
    echo "Started batch with execution id '${executionId}'"
    response=$(curl --cookie ${curlCookieJar} --cookie-jar ${curlCookieJar} ${loadbalancerUrlToLocalhost} --header "Content-Type: application/json" ${restUrl}/poll?executionId=${executionId} 2>/dev/null)
else
    echo "Failed to start batch"
    response=16
fi
#echo "Response: ${response}"
if ! [[ "$response" =~ ^-?[0-9]+$ ]]; then
	echo "failed to start batch"
	if [ -f ${serverLog} ]
	then
		grep 'Caused.*BatchException' ${serverLog} | tail -1
	else
		echo "Server log not found at location '$serverLog', so the cause of error could not be printed. Use parameter -l to set the location of the server log."
	fi
	echo "returning with exit code 16"
    # cleanup
	exit 16
fi
#check status and wait if status is running
until [ "$response" != "-1" ]; do
	sleep "$pollSleep"
	response=$(curl --cookie ${curlCookieJar} --cookie-jar ${curlCookieJar} ${loadbalancerUrlToLocalhost} --header "Content-Type: application/json" ${restUrl}/poll/${executionId} 2>/dev/null)
done

#RETURN BATCH STATUS
# echo "batch status : $response"
# cleanup
exit ${response}
