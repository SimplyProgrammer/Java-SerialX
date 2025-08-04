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
  * MapProtocol - to serialize maps!
  * ScopeProtocol (reading only) to read scopes using protocol!
  * AutoProtocol - will automatically serialize selected fields with getters and setters!
  * EnumProtocol - to serialize any java enum!
  * SelfSerializableProtocol - operates with SelfSerializable interface!
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
    ##
    * As mentioned. you can create your own parsers or even replace already existing ones with yours!
  * SerializationProtocol API - long known protocol system for more complex objects. It contains 8 protocols as before! Now protocols are operated by ObjectConverter!
* New import system that allows you to import some class once with certain alias and then use it with that alias, similar  to java!
* Too big integers are now automatically converted into long without necessarily of using L suffix!
* Small new syntax features and alot of small enhancements (shortened version of variable being initialized to scope)!
* Alot of string utility methods from Serializer become public and some were moved into converters where they are mainly used!
* Registry object which is Collection type that can store only one instance per class!
* Some new functions in Scope!
* Deprecated methods were removed!
* Source code was excluded from main jar to save space and is now available in separate src.zip file! Now on java doc files will not be provided and src.zip should be used instead!
* Small bugs fixed but there were alot of internal changes in this update so they might be another bugs so I encourage you to report any bug you encounter!
#

# SerialX 1.3.2

Release date: 10.25.2021 (Morning) 

What was added: 
* Serializer now abstract class which inherits Scope so now it is Scope that can serialize itself! Serialization and deserialization methods are now not static and original functionality has been split into two separated objects that inherit Serializer:
  * JussSerializer - which is responsible for serializing and deserializing objects using Juss format (original functionality of Serializer).
  * JsonSerializer - which is responsible for serializing and deserializing objects using Json format (successors of JsonSelxUtils)
* JsonSelxUtils was replaced with JsonSerializer that is capable of both reading and writing Json!
* Main formatting and reading algorithms can be now overridden by extending JsonSerializer, JussSerializer or Serializer!
* Ability to set multiple variables on one value, for example x = y = z = 5
* Ability to remove multiple variables by setting them on null!
* Variables of scope are now settable from outer world, for example someScope.x = 9
* Compare identity operator (triple equals) was added and transtype comparison logic was changed, mainly between primitive datatypes!
* Logical operators now have higher precedence over comparison operators by default!
* Logic behind operators can now be overridden by extending belonging operator DataParser!
* Adding some new utility and functionalities!
* Small syntax features (scopes now don't have to be separated with semicolon if they are in new line)!
* Package name was renamed from "ugp.org.SerialX" to "org.ugp.serialx"!
* Fixing some bugs with formatting and reading!
#

# SerialX 1.3.5

Release date: 8.30.2022 (Night)

What was added:
* Scope was split into 2 separate classes:
  * GenericScope - that allows you to set generic types of keys and values. Furthermore, it can be serialized with generic types preserved!
  * Scope - that you already know which poses the same functionality as before now as a child class of GenericScope!
* Imports system was redesigned and splitted into multiple separate classes, each handling some part of functionality!
  * Also imports are now Serializer specific rather than global!
* Precedence of ConditionalAssignmentOperators ?: and ?? was slightly altered to closely resemble behavior of these operators in other languages. Also, these operators now can be nested without necessity of ().
* Parser API (DataParser and DataConverter) was redesigned and is now handled by ParserRegistry which can provide additional functionality such as caching to improve performance!
* Serialization syntax of Serializable objects using Base64 via SerializableBase64Converter was slightly altered to mitigate conflicts with the rest of JUSS syntax!
* New "from/into API" which is now part of the Scope that allows you to map almost any java object into a scope and any scope into corresponding java object!
* AutoProtocol is now based on "from/into API" making it more flexible!
* New UniversalObjectInstantiationProtocol that can deserialize any object by calling its constructor (something similar to ObjectClass::new)!
* SerializationProtocols now have a "mode" that can define what they can do!
* JsonSerializer will now serialize JUSS protocols as JSON objects to achieve more JSON compatibility out of the box!
* LogProvider which is now responsible for logging errors and allows you to implement your own form of logging!
* SerializationDebugger that provides ability to debug serialization and deserialization!
* New utility across API and small new functionalities and changes!
* Fixing bugs (hopefully not adding new ones):
  * Long live bug with // and /* comments in strings now fixed for good (I hope...)
  * Bug with wrong formatting when serializing Json in Juss and revers!
  * Some other small ones!
* New examples were added!
#

# SerialX 1.3.8

Release date: 9.22.2024 (Night)

What was added:<br>
### Maven:
* The whole library was modularized using Maven into the following modules:
  * SerialX-core - Contains core features and utilities shared across the library. It also contains basic protocols and parsers that resemble the functionalities of pre-1.2.X SerialX.
  * SerialX-juss - Now contains everything JUSS related, features that were added roughly in 1.2 and later... This includes things like JussSerializer, ArrayConverter, OperationGroups etc...
  * SerialX-json - A relatively small extension of the JUSS module that is more narrowly focused on JSON. This is now where SerialX support for JSON is located.
  * SerialX-operators - An extensional module, this is now where all operator parsers are located.
  * SerialX-devtools - Small module containing tools for debugging the library, mainly Parser/Converter API. It is intended for DSL developers and people who want to add their own data formats.
* From now on Maven will be used for dependency management and building of this library.
* Distribution of this library will be conducted using Maven from now on.
  * This greatly simplifies a lot of lengthy, complex IDE-specific processes.

### Unit tests and benchmarks:
* Some examples are now used as unit tests, this should greatly simplify the testing process and reduce the chance of bug introduction in the future.
* These are now located on the new "tests-and-experimental-features" branch. This branch will be used for demonstrations, benchmarking, testing and experimenting. Note that this branch is not part of the main API.

&nbsp;

### Specific changes:
* Library is now compatible with JDK versions up to 21 (probably even newer but it was not tested)!
* ImportsProvider now implements caching for Imports.
* ParserRegistry now implements DataParser allowing for easier creation of more complex (context-free) languages.
* NumberConverter was refactored, now providing all in one parsing numberOf function that is on average 12x faster than the old implementation.
  * New numberOf function supports octadecimal numbers and has improved handling of E-notation.
  * Java integer caching is now in place.
  * DecimalFormater was dumped in favor of the more customizable overridable format method.
* BooleanConverter and NullConverter were slightly refactored allowing for near O(1) complexity of parsing.
* ObjectConverter got a significant refactor!
  * It was separated into 2 separate classes across 2 modules. Now it is ProtocolConverter that is extended by ObjectConverter.
  * Static member invocation is now only allowed on a small carefully selected group of classes, fixing the major security (arbitrary code execution) exploit that would allow an attacker to call any static function of any class in a hypothetical REST implementation scenario. Note that this is only a mitigation that allows you to whitelist the specific classes for static member invocation so the maximal caution is still advised when doing so.
  * Both ObjectConverter and ProtocolConverter were slightly optimized.
* StringConverter was slightly optimized by introducing caching. It is disabled by default, by enabling it the same String instance will be returned for the same strings during parsing.
  * Static variables were made instance-specific allowing for more flexibility.
* GenericScope and Scope received various API improvements, the most notable ones being:
  * GenericScope now implements Collection instead of just Iterable making it part of Java collection API.
  * From/Into API now partially supports recognition for generic types of declared Object<T> variables, making it more useful for non-JUSS formats such as JSON. This enhances also AutoProtocol and UniversalObjectInstantiationProtocol together with SelfSerializableProtocol.
* Mode of SerializationProtocol is now implemented with 64 int (long) bit-packing which allows for chaining of multiple modes which can now be understood as protocol types. This trades an overall number of unique modes for greater utility. 
* Serializer (and core high-level changes):
  * The concept of scope parent variable inheritance was abandoned due to being unacceptable and inefficient (both time and space-wise...), quite error-prone and tedious to work with as well as and potentially dangerous. Not mentioning the fact that the only reason for its existence was to allow you to access variables declared in the parent scope, for which it suboptimal solution to say at least...
  * In a similar fashion, the notion of each parser having to return the new instance of the respective object for every parsed string was abandoned as well and is no longer required, allowing for more flexibility and concepts such as already mentioned caching!
  * OOP NULL was abandoned as well as it was a biproduct of sub-optimal decisions mentioned above and therefore conceptually flawed. It is deprecated and should not be used!
* All static utility functions (for instance string analyses and processing functions) were separated into new Utils class, in order to achieve better separation of concerns...
  * All string analyses and processing utility functions (for example indexOfNotInObj or splitValues) were rewritten into more "finite state machine"-like form which slightly increases their performance...
  * Some new utility functions were added and existing ones were improved!

&nbsp;
* Operation group mark of OperationGroups (parentheses operator) is no longer static and always the same, now it is shorter and pseudo-randomly generated which slightly increases the performance and highly mitigates the hypothetical "Group mark injection" attack which would result in unauthorized access to group mark at runtime.
* UniversalObjectInstantiationProtocol is no longer registered by default and now requires manual registration for a specific class, this enhances security since instantiation of any object (dangerous or not) is prevented.
  * The same goes for SelfSerializableProtocol.

&nbsp;
* JsonSerializer received many improvements and bug fixes in order to match JSON more accurately.
  * Dedicated JsonCharacterConverter and JsonNumberConverter were added for this. Also, JsonObjectConverter was separated into its own class. 

&nbsp;
* Operators class was added, which is used for injecting all operator parsers into the specific registry.
* Almost every operator parser was refactored, resulting in shorter, far simpler and therefore more optimized code. The most notable ones are:
  * ArithmeticOperators which now also allows you to declare your own arithmetic operators and/or specify their precedence.
  * Due to refactoring, ResultWrapper is no longer needed and will be removed, this extends to LogicalOperators as well.
* NegationOperator now supports separate handling of logical negation (! operator) and mathematical negation (- operator), however by default their behavior is the same.

&nbsp;
* The ability to specify custom output PrintWriter was added for SerializationDebugger.

&nbsp;
* Besides the changes mentioned above, countless smaller improvements including numerous bug fixes, performance improvements, API enhancements or Javadoc specifications were added across the whole library.
  * Some functions were slightly renamed but it is usually documented but deprecated functions were removed!
#

# SerialX 1.3.9

Release date: 8.4.2025

### Notable changes:
* A new, far performant, reading and lexing method was written for parsing, resulting in almost 2x improvement when deserializing a large amount of data.
* Serializer now provides an option to implement a custom code formatting flag, including the option to disable formatting entirely (so no indentation or new line blank characters will be present).
* DataConverter `CharSequence toString(ParserRegistry myHomeRegistry, Object obj, Object... args)` was changed to `Appendable toString(Appendable source, ParserRegistry myHomeRegistry, Object obj, Object... args) throws IOException` where now, instead of creating the string from the object and returning it, you are supposed to append it directly into the provided source. In this way, you do not have to create your own StringBuilders.
 * This is far more optimal, and together with formatting flags, results in 2x, sometimes even 3x increase in performance when serializing, especially with larger amounts of data.
* Key Serializer methods, LoadFrom and SerializeTo were renamed to camel case (original PascalCase was largely a legacy thing...).
* JsonVariableConverter was added as JSON-specific variant of VariableConverter.
* ArrayConverter now supports proper serializing of 0 and 1 length arrays (@ identifier for arrays).
* Slight default formatting changes (';' is no longer used for the last element, similarly to JSON).
* Other smaller optimizations and API improvements.
 * hashCode for GenericScope
 * Deprecateds were removed...
 * More Unit/Integration tests...
* Minor bug fixes and Javadoc improvements.

# SerialX 1.4.0

Release date: ???

### Roadmap of potential changes:
* Separating From/Into API from Scope and Serializer into the standalone classes making it more flexible and customizable.
* ? Implementing Register in such a way that it will be possible to specify the underlying data structure.
* ? ParserProvider, ProtocolProvider, MultimediaSerializer interfaces...
* ? Optimizations - Introducing char/class-based direct dispatch hashing algorithm for selecting the best fitting data converter for the string/object. (This is likely not going to be possible to fully implement due to internal dependencies between parsers and other legacy reasons...)
* ? Java 11

