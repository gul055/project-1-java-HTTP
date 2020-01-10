#!/bin/bash

java -cp build/jar/httpd.jar:lib/ini4j-0.5.5-SNAPSHOT.jar ServerMain $@
