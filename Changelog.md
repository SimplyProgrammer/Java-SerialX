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
* Scope is the physical manifestation of loaded content in your program!
* Ability to create sub-scopes / neasted scopes in side of parent scopes or file itself similarly to JSON! For example: { \/\*scope\*\/ }
* Ability to serialize string normaly in quotes like in java! But certain syntactical characters from SerialX cant be present!
* "splitValues" method was removed becasue it was out of purpous of library itself.
* Comma now works as semicolon!
* Tremendous reading performence boost! Large quantity reading is now up to 50x faster than in previous version.
* Fixing a bug when order of elements being messed up during serialization.
* Fixing a bug with hexadecimal and binary number formats.
* Fixing some other less important bugs.
* Note: Since this is pre release, there are probably some bugs but hopefully nothing totaly broken. Also this prerelease can only read scopes, not write!
#

# SerialX 1.2.2

Release date: 4.11.2021 (Afternoon)

What was added:
* Ability to serialize Scope object!
* Ability to clone Objects using Serializer!
* Ability to instantiate any object using Serializer by calling shortest public constructor!
* Now you can access Java utility from SerialX, you can invoke public static methods and fields directly from SerialX!
* SelfSerializable interface which gives you ability to serialize objects without protocol by calling public constructors!
* Static field "new" to obtain clone of variable and "class" to obtain class of variables value!
* 4 new protocols:
* * MapProtocol - to serialize maps!
* * ScopeProtocols (reading only) to read scopes using protocol!
* * AutoProtocol - will automatically serialize selected fields with getters and setters!
* * EnumProtocol - to serialize any java enum!
* * SelfSerializableProtocol - operates with SelfSerializable interface!
* Tremendous writing performance boost! Large quantity writing is now up to 80x faster than in previous version.
* Eliminating usage of Regex completely which results into even faster reading!
* Now you can access variables of scopes by "." directly in SerialX!
* Fixing bug when blank characters despair from string, also now string can contains any character except quote and nextline!
* SerialX API is now partially opensource, the sources are included in main Jar, however according to the License you cant appropriate any of this code without including its origins!
#

# SerialX 1.2.5

Release date: 4.11.2021 (Afternoon)

What was added:
* Serializer can now serialize into any Appendable which includes all Writers, StringBuilder and many others which gives you a lot of opportunities!
* Serializer can now read from any CharSequence or any Reader object!
* Serializer is now fully capable of reading JSON!
* Serializer can read specific object or variable while ignoring any other stuff which saves a lot of performance (this is experimental)!
* Slight increase of reading performance!
* Utility to work with JSON like JsonScope!
* A lot of new utility in Scope object such as filtering or object transformation!
* Small bugs fixed!
#

# SerialX 1.3.0

Release date: 8.8.2021 (Night) 

What was added: 
* Revelation of compiler that is now Recursive descent parser that is customizable and configurable.
* Structure of entire API was generally reorganized in to 3 main sections:
  * Serializer - which is main class that operates entire API. Is responsible for input and output, formatting and general utility!
  * DataParser and DataConverter API - is recursive  descent parser itself that is responsible for converting objects to strings and parsing them back! In default SerialX API implementation now known as JUSS (Java universal serial script) are these parsers and converters available:
    * NumberConverter - for converting and parsing numbers (integers, decimals, hexa, bin)!
    * BooleanConverter - for converting and parsing booleans!
    * CharacterConverter - for converting and parsing chars!
    * StringConverter - for converting and parsing strings ("Hello world!", "And others...")!
    * NullConverter - for converting and parsing null!
    * ObjectConverter - for converting and parsing SerializationProtocol expressions and Scopes!
    * VariableConverter - for converting and parsing JUSS variables (Map.Entry)!
    * SerializableBase64Converter - for converting and parsing Base64 expressions (java.io.Serializable)!
    * ArrayConverter - for converting and parsing primitive arrays!
    ##
    * OperationGroups - for parsing expression groups such as (5 + 5) / 2
    * ArithmeticOperators - for parsing arithmetic expression such as 2 + 5 * 4 ** 2
    * LogicalOperators - for parsing logical expression such as true && false || true
    * ComparisonOperators - for comparing objects, for instance 6 > 5
    * ConditionalAssignmentOperators - that provides ternary operator (?:) and null coalescing (??)
    * NegationOperator - to negate stuff, for example !true
  * SerializationProtocol API - long known protocol system for more complex objects. It contains 8 protocols as before! Now protocols are operated by ObjectConverter!
* New import system that allows you to import some class once with certain alias and then use it with that alias, similar  to java!
* Too big integers are now automatically converted into long without necessarily of using L suffix!
* Small new syntax features and alot of small enhancements!
* Alot of string utility methods from Serializer become public and some were moved into converters where they are mainly used!
* Registry object which is Collection type that can store only one instance per class!
* Some new functions in Scope!
* Deprecated methods were removed!
* Source code was excluded from main jar to save space and is now available in separate src.zip file! Now on java doc files will not be provided and src.zip should be used instead!
* Small bugs fixed but there were alot of internal changes so I encourage you to report any bug you encounter!
#
