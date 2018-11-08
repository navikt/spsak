#!/usr/bin/env bash

ssoCookieName="nav-isso"
lbCookieName="amlbcookie"

## Exception handling
# https://gist.github.com/coderofsalvation/8268365
declare -A LISTENERS

throw(){
  EVENT=$1; shift; for listener in "${LISTENERS[$EVENT]}"; do eval "${listener} $@"; done
}

addListener(){
  if ! test "${LISTENERS['$1']+isset}"; then LISTENERS["$1"]=""; fi
  LISTENERS["$1"]+="$2 " # we can get away with this since functionnames never contain spaces
}

# convert exitcodes to events
trap "throw EXIT"    EXIT
trap "throw SIGINT"  SIGINT
trap "throw SIGTERM" SIGTERM

function extractHostFromLoadbalancerUrl
{
# extract the protocol
proto="$(echo $1 | grep :// | sed -e's,^\(.*://\).*,\1,g')"
# remove the protocol
url="$(echo ${1/$proto/})"
# extract the user (if any)
user="$(echo $url | grep @ | cut -d@ -f1)"
# extract the host
host="$(echo ${url/$user@/} | cut -d: -f1 | cut -d/ -f1)"
# by request - try to extract the port
port="$(echo $host | sed -e 's,^.*:,:,g' -e 's,.*:\([0-9]*\).*,\1,g' -e 's,[^0-9],,g')"
# extract the path (if any)
path="$(echo $url | grep / | cut -d/ -f2-)"

    echo ${host}
}

function propertyValue
{
    local property=$1
    if [ ! -f ${propertiesFil} ]; then
        echo "Fant ikke propertiesfil som er påkrevd for å hente ut properties"
        exit 16
    fi
    echo $(grep -i ${property} ${propertiesFil} | awk -F'=' '{print $NF}')
}

function jsonValue()
{
    local KEY=$1
    awk -F"[,:}]" '{for(i=1;i<=NF;i++){if($i~/'${KEY}'\042/){print $(i+1)}}}' | tr -d '"' | sed -n 1p
}

function issoLogin()
{
    local username=$1
    local password=$2
    local authUrl="$3json/authenticate"
    local cookieJar=$4

    if [ ! -f ${cookieJar} ]; then
        touch ${cookieJar}
    fi
    if [ $(stat -c "%a" "$cookieJar") -ne "600" ]; then
        chmod 600 ${cookieJar}
    fi

    # Auth
    local authCallback=$(curl --silent --cookie ${cookieJar} --cookie-jar ${cookieJar} --request POST --header "Content-Type: application/json" --header "Authorization: Negotiate" ${authUrl} | sed -e 's/"name":"IDToken1","value":""/"name":"IDToken1","value":"'${username}'"/g' -e 's/"name":"IDToken2","value":""/"name":"IDToken2","value":"'${password}'"/g')
    local authToken=$(curl --silent --cookie ${cookieJar} --cookie-jar ${cookieJar} --request POST --header "Content-Type: application/json" --data "${authCallback}" ${authUrl} | jsonValue tokenId)

    # Insert cookie into cookie-jar
    grep -i "${lbCookieName}" ${cookieJar} | sed -e 's/'${lbCookieName}'.*/'${ssoCookieName}'\t'${authToken}'/g' >> ${cookieJar}
}

function issoLogout()
{
    local authUrl="$1json/sessions/?_action=logout"
    local cookieJar=$2
    local issoCookie=$(grep -i nav-isso ${cookieJar} | awk '{print $7}' )
    local logoutResponse=$(curl --silent --request POST --header "nav-isso: ${issoCookie}" "$authUrl" | jsonValue result)
    rm ${cookieJar}
}
