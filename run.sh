#!/bin/zsh

# Pass in "airplane" as an argument for airplane mode
mvn spring-boot:run -Dspring-boot.run.profiles="dev,$1"