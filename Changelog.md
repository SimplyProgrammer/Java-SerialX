# SerialX 1.0.0 (beta)

Release date: 8.14.2020 (Evening)

What was added:
* Serializer utility class.
* 2 build-in protocols.
#

# SerialX 1.0.5 (beta)

Release date: 8.20.2020 (Night)

What was added:
* Ability to generate comments.
* Fixing some small bugs.
#

# SerialX 1.0.6

Release date: 8.22.2020 (Noon)

What was added:
* Improveing way to serialize numbers.
* Adding suffixes fot double "d", short "s" and byte "y"!
* Java Base64 serialized object now does not need to start with "#"!
* Objects serialized using protocol with no arguments will now not by serialized with null argument!
* Repairing an error with long suffix!
#

# SerialX 1.1.0

Release date: 9.22.2020 (Afternoon)

What was added:
* Adding decimal number formatter!
* "unserialize" method in SerializationProtocol now throws Exception which makes reflection stuff easier!
* Fixing some problems such as "Too big objects simply disappear during serialization!"
* Better optimization. Improving the performance of Serializing and Unserializing astronomically!
* Some characters now can be serialized using regular Java way for example 'a'! However SerialX syntax characters such as { or } must be still serialized using ASCII code!
Numbers must have additional character behind for example '4/' otherwise they will be taken as ASCII code! 
* Some new methods and stuff!
#

# SerialX 1.1.2

Release date: 9.27.2020 (Evening)

What was added:
* Integers now can be serialized using Java binary and hexadecimal form (0b1111, 0xffff)!
* Numbers can be separated with underscore (just like in Java)!
* Fixing the bug when formatter mess up decimals suffixes and integers!
#

# SerialX 1.1.5

Release date: 12.6.2020 (Evening)

What was added:
* Variable system! Now "order" is not only possibility.
* Functions working with variable system!
* Functions that allows you to insert custom code (comments and stuff)!
* Fixing "long-lived" bugs such as the on with double slash comment, hopefuly for the last!
#

# SerialX 1.2.0_pre - V2

Release date: 3.18.2021 (Afternoon)

What was added:
* New Scope object that are now values and variables loaded in to so now its not necesarry to load indepednent values and variables separatly!
* Scope is not physical manifestation of loaded content in your program!
* Ability to create sub-scopes / neasted scopes in side of parent scopes or file itself similarly to JSON! For example: { \/\*scope\*\/ }
* Ability to serialize string normaly in quotes like in java! But certain syntactical characters from SerialX cant be present!
* "splitValues" method was removed becasue it was out of purpous of library itself.
* Comma now works as semicolon!
* Trumendous reading performence boost! Large quantity reading is now up to 50x faster than in previous version.
* Fixing a bug when order of elements being messed up during serialization.
* Fixing a bug with hexadecimal and binary number formats.
* Fixing some other less important bugs.
* Note: Since this is pre release, there are probably some bugs but hopefully nothing totaly broken. Also this prerelease can only read scopes, not write!
#
