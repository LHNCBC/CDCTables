# CDCTables
Pull data from RxNorm and RxClass APIs to produce preliminary tables for the CDC opioid database

## Package the executable jar file with dependencies
```
mvn package
```

## Generate the data for load using the jar
Move the packaged jar to the working directory.  The working
directory should contain the config folder as a subdirectory.
Outputs will be written to the working directory.
```
> java -jar CDCTables-*.jar

[1] Reading configuration files
  - from ./config/filtered_RxNorm-msp.txt ...OK
  - from ./config/rx2ICD.txt ...OK
[2] Fetching ATC Classes
[3] Collecting edges of ATC classes for isa relations
[4] Processing RxNorm substances and asserting relations
  Processed 500 substances of 12287
  Processed 1000 substances of 12287
  Processed 1500 substances of 12287
  Processed 2000 substances of 12287
  Processed 2500 substances of 12287
  Processed 3000 substances of 12287
  Processed 3500 substances of 12287
  Processed 4000 substances of 12287
  Processed 4500 substances of 12287
  Processed 5000 substances of 12287
  Processed 5500 substances of 12287
  Processed 6000 substances of 12287
  Processed 6500 substances of 12287
  Processed 7000 substances of 12287
  Processed 7500 substances of 12287
  Processed 8000 substances of 12287
  Processed 8500 substances of 12287
  Processed 9000 substances of 12287
  Processed 9500 substances of 12287
  Processed 10000 substances of 12287
  Processed 10500 substances of 12287
  Processed 11000 substances of 12287
  Processed 11500 substances of 12287
  Processed 12000 substances of 12287
[5] Adding misspellings
[6] Adding T-codes
[7] Serializing table files
Finished data serialization in 812 seconds.
```
## Load the data to the opioid database
The script will pull the source data from the directory it is run
```
mysql --local-infile=1 -u root -p opioid < load_opioid_data.sql
```