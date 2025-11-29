# CMPT 276 Project – Team 6 Game

## Team Members

1. Sultani, Besmillah – <bsa106@sfu.ca>
2. Presto, Caleb – <cpresto@sfu.ca>
3. Mirzada, Rahim – <rkm12@sfu.ca>
4. Mock, Kai Lun Jason – <klm28@sfu.ca>

## Overview

This project is a 2D grid based game written in Java.  
The player starts on the left side, collects required rewards,
avoids enemies and punishments, and then reaches the exit to win.

- Source code is in `src/main/java/com/project/team6`  
- Tests are in `src/test/java/com/project/team6`

## Requirements

- Java 17  
- Maven 3  

## Build and Run

Go to the `team6game` directory (where `pom.xml` is) and run:
> mvn clean compile exec:java  
This compiles the code and starts the game window.

## Tests and Coverage

Go to the `team6game` directory and run:
> mvn clean test  
This compiles the code and runs all JUnit tests.  
JaCoCo also runs and creates a coverage report at:
`target/site/jacoco/index.html`
Open this file in a browser to see the coverage report.

## JAR File and Javadocs

To build a runnable JAR from `team6game`:
> mvn clean package  
This creates: `target/team6game-1.0-SNAPSHOT.jar`  

Run it with:
> java -jar target/team6game-1.0-SNAPSHOT.jar  

To generate Javadocs, run:
> mvn javadoc:javadoc  

The HTML docs are in `target/site/apidocs`

## Tutorial / Demo Video

We also provide a short tutorial and demo video that shows how
the game is played and explains the main features.

Tutorial / demo video links:
[1]  Losing scenario: <https://www.youtube.com/watch?v=Gktyp7l3N6c>  
[2]  Winning scenario: <https://www.youtube.com/shorts/2SCb2KY5Y0M> 
