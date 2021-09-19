# README
To build use:
`mvn package`

To run use:
`./run`

This will give you a barebones REPL, where you can enter text and you will be output at most 5 suggestions sorted alphabetically.

To start the server use:
`./run --gui [--port=<port>]`

Project details
### Stars

## Description

Supports two commands: 

## stars <filename>
Loads data containing star information from a CSV file, specified by <filename>,

## Contributors
Aaron Jeyaraj (ajeyaraj)

## Estimated Time Taken
10 hours

### Design Choices
Implemented two additional classes:
## 1) Star
Represents a star in the data, contains several fields which record the star's id, name, and x, y, and z coordinates. 
## 2) StarList
Used to maintain a collection of stars. In the Main class, the `stars` variable is used to store a `StarList`.
Contains the following fields:
`records`: an ArrayList of stars
`nameMap`: a HashMap which maps the names of stars to the star

Contains the following methods:
`findNeighboursByName`: given k and a name of a star, returns an array of the closest Stars to the given coordinates, ordered by distance
`findNeighboursByCoordinates`: given k and a set of coordinates, iterates through Stars stored in `records` and returns an array of the closest Stars to the given coordinates, ordered by distance
`loadStars`: given a filename representing the file containing the star data, checks for the validity of the file, and then populates `records` and `nameMap` with the necessary information
`getDistance`: given two sets of 3-dimensional coordinates, returns the Euclidean distance between the points

Tests --

To build use:
`mvn package`

To run use:
`./run`

This will give you a barebones REPL, where you can enter text and you will be output at most 5 suggestions sorted alphabetically.

To start the server use:
`./run --gui [--port=<port>]`
