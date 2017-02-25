#!/bin/bash
nohup java -jar -Dspring.profiles.active=prod,swagger -Dserver.port=80 -Dmanagement.security.enabled=true selectdata-0.0.1-SNAPSHOT.jar 2>error.log 1>/dev/null &



