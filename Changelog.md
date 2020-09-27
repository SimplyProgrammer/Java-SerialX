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
