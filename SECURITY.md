# Security Policy

## Versioning schema
This project uses incremental X.Y.Z version number (XYZ are numbers from 0 to 9) where:
* X = "Uber version", this is likely to stay 1 forever. It is also quiet possible that numbers are going to be shifted 1 space towards the left for this exact reason...
* Y = Major version. When this number is incremented it usually means something "revolutionary" was added, something that moved the library forward significantly. Moderate migration is often times required...
* Z = Majnor version. Although it is called "minor", it can oftentimes be quiet big. The bigger the number difference between the last minor version, the bigger the update. But the library is usually compatible between minor version with minimal migration needed (deprecateds or changes are usually documented in the code...).

## Supported Versions
There are no LTS versions, except the current version. It is recommended to always use the latest version if possible as the old versions were slow, riddled with bugs and sometimes quite significant security vulnerabilities.
Also is highly discouraged to use any "alpha", "beta" or versions that end with _SNAPWHOT.

## Reporting a Vulnerability

Currently, there are no known vulnerabilities present in the library but still be wise with your code, mainly with creating protocols and parsers, if your work is security-focused! 
You can report responsibilities using [Issues section](https://github.com/SimplyProgrammer/Java-SerialX/issues).
