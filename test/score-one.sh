#!/bin/bash

HOST=localhost
PORT=3000

COLL=SEPdataset
FILE="Dynamic_Gift_Content_Final_assembled_a-Plain_Text-1000.txt"

URL="http://${HOST}:${PORT}/${COLL}/${FILE}/"

echo curl "$URL" 
curl "$URL"  | python json-print.py

