#!/bin/bash
echo "PST 메일 검색 서비스를 시작합니다..."
java -Xmx4g -Xms512m -Djava.awt.headless=true -jar pst-search.jar
