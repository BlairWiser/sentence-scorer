#!/bin/bash

HOST=localhost
PORT=3000

COLL=SEPsamples

URL="http://${HOST}:${PORT}/${COLL}/"

URL=`echo $URL | sed "s/ /%20/g"`

curl "$URL" | python json-print.py


