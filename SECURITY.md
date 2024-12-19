# Security Policy

## Versioning schema
This project uses incremental X.Y.Z version numbers (XYZ are numbers from 0 to 9) where:
* X = "Uber version", this is likely to stay 1 forever (and therefore is kind of useless). It is also quiet possible that numbers are going to be shifted 1 space towards the left for this exact reason.
* Y = Major version. When this number is incremented it usually means something "revolutionary" was added, something that moved the library forward significantly. Moderate migration is likely to be required...
* Z = Majnor version. Although it is called "minor", it can oftentimes be quiet big. The bigger the number difference between the last minor version, the bigger the update. But the library is usually compatible between minor version with minimal migration needed (deprecateds or changes are usually documented in the code...).

## Supported Versions
There are no LTS versions, except the current version. It is recommended to always use the latest version if possible as the old versions were slow, riddled with bugs and sometimes quite significant security vulnerabilities.
Also is highly discouraged to use any "alpha", or "beta" versions.
Using versions with _SNAPSHOT at the end is only recommended when it is the latest version available. Keep it mind that the correct functionality in these versions is not guaranteed and they should be updated to the latest (non _SNAPSHOT) version/release as soon as it becomes possible.

## Reporting a Vulnerability

Currently, there are no known vulnerabilities present in the library but still be wise with your code, mainly with creating protocols and parsers, if your work is security-focused!<be>
After all, and also unfortunately, this library has its history of security missteps so reporting potential vulnerability of any kind is encouraged (preferably without "spreading the word" too much).
You can do so by using [Issues section](https://github.com/SimplyProgrammer/Java-SerialX/issues).
