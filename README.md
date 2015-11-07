# LogLibrary

## The Task: ##

- Make Logger library that acts like real Logger (like log4j, slf4j and so on). 
The main functionality in LogLibrary is to make logs and to write these logs in file. The log would look like something like this:

Format:   
  <code> YYYY.MM.DD HH:MM:SS:SSS log</code>
  
Example:  
  <code> 2015.07.11 14:44:56:567 log </code>
- If the size of the log file become > 1MB - make .zip file with this file.
- Make possible to find particular log by Date in above format - <code> YYYY.MM.DD HH:MM:SS:SSS </code>
