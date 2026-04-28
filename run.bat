@echo off
chcp 65001 > nul
echo PST 메일 검색 서비스를 시작합니다...
echo.
java -Xmx4g -Xms512m -Djava.awt.headless=true -jar pst-search.jar
pause
