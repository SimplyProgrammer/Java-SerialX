# Java-SerialX
SerialX is powerful and lightweighted utility library to serialize objects in Java. Serialization means storing Java objects and values into some media (for example text file).<br>
SerialX is improving regular Java Base64 serialization and adding serialization protocols that you can create for objects that cant be serialized using regular way. For example final non-serializable objects, 3rd party objects and others. SerialX API is storing objects into JSON like "programming" language (data format) called JUSS (Java universal serial script) which shares common functionality with JSON and provides more customizability and extended functionality! This allows you to serialize multiple objects into one string or also into file. But unlike to JSON, JUSS general conception is based on determinate order of arguments or values we can say. The latest versions also provides variable system (keys, values) similar to JSON. But in JUSS these variables can be overided and can interact with each other and can be used multiple times. Nowadays SerialX provides recursive descent parser that can be modified so you can create your own data structures! In other words SerialX allows you to serialize **anything**, it's pretty simple to use and practically limitless!
## Brief overview of working concept and advantages compared to regular serialization:
**Regular java serialization** is strongly based on some kind of "magic" or we can say "godly reflection" which will reflectivly read all fields of object including private and final ones and then interprets it as Base64 string. And during deserialization it will create an empty instance of object absolutely ignoring its constructors by using some "magic" compilator process to create it instad, and then it will violently write all serialized fields again including private and final ones which is realy not the best approach! Also, this allows you to serialize only instances of java.io.Serializable and all fields must be instances of Serializable as well which is also not the most useful thing! <br>
Compared to this, **SerialX API** is doing everything programmatically. SerialX API uses ``SerializationProtocol``s that are registered in ``ProtocolRegistry``, each working for certain class! ``SerializationProtocol`` contains 2 methods, ``serialize(T object)`` and ``unserialize(Object[] args)``. ``serialize(T object)`` method obtains certain object to serialize and its job is to turn this object into an array of objects that we can then reconstruct this exact object from, such as constructor arguments! These arguments are then paste into ``Serializer`` and ``Serializer`` serialize them into mentioned SerialX API data storage format. During deserialization, ``Serializer`` first takes given data serialized in SerialX, unserializes them into array of objects and this array is then paste into ``unserialize(Object[] args)`` method of certain ``SerializationProtocol`` as argument. The job of ``unserialize(Object[] args)`` method is to create an new instance of serialized object ``T`` from given arguments! Everything in this function is controlled by you and you can write them by yourself which gives you an absolute control! <br>
Note: Since 1.3.0, protocols are operated by DataParsers and are mainly used for more complex objects. Also Object to String conversion is now done by DataConverter and String - Object is done by DataParsers and further by protocols!
**Advantages and goals:**
* Overcoming most of regular serialization problems such as bypassing constructor!
* Powerful and highly costomizable/opinionated, you have control over stuff via protocols and recursive descent parser!
* Programmaticall, meaning you can decide how objects will be serialized and deserialized!
* Fast, SerialX solution is almost always far more faster than regular serialization!
* Readable, It depends but SerialX formats are supposed to be pretty readable for humans and should be also pretty intuitive for learning and writing!
* Data types recognition, SerialX defaultly supports all primitive datatypes from Java and also objects (done with protocols) compare to Json for instance!
* Small storage requirements, as you can see belove SerialX is often times far smaller than Json not even mentioning XML!
* Quantity, SerialX can serialize multiple objects into one file or string!
* Fully compatible and interoperable with JSON!
* Very easy to use, at the beginning all what you need to know is ``JsonSerializer#SerializeTo(file, objects)`` for serializing and ``JsonSerializer#LoadFrom(file)`` for deserializing!
* Recursive descent parser that is fully customizable and can be used to parse and convert potentially anything from JSON to CSS!
* Lightweight, all modules combined under 150KB!

## Comparison: XML (.xml) vs Json (.json) vs YAML (.yml) vs JUSS (.juss or .srlx)
Sample object:
```
public class Foo
{
  double val1 = 55, val2 = 455.45;
  float val3 = 236.12F;
  boolean flag = true;

  public double getVal1()
  {
    return val1;
  }
  public void setVal1(double val1)
  {
    this.val1 = val1;
  }
  public double getVal2()
  {
    return val2;
  }
  public void setVal2(double val2)
  {
    this.val2 = val2;
  }
  public float getVal3()
  {
    return val3;
  }
  public void setVal3(float val3)
  {
    this.val3 = val3;
  }
  public boolean isFlag()
  {
    return flag;
  }
  public void setFlag(boolean flag)
  {
    this.flag = flag;
  }
}
```
##
Serialized via **XMLDecoder for XML:**
```
<?xml version="1.0" encoding="UTF-8"?>
<java version="1.8.0_92" class="java.beans.XMLDecoder">
    <object class="some.package.Foo">
        <void property="val1">
            <double>55</double>
        </void>
        <void property="val2">
            <double>455.45</double>
        </void>
        <void property="val3">
            <float>236.12</float>
        </void>
        <void property="flag">
            <boolean>true</boolean>
        </void>
    </object>
</java>
```
<br>Serialized via **JACKSONE (hypothetical) for Json:**
```
...
{
  "val1" : 55.0,
  "val2" : 455.45,
  "val3" : 236.12,
  "flag" : true 
}
```
<br>Serialized via **(hypothetical) YAML:**
```
val1: 55.0
val2: 455.45
val3: 236.12
flag: true 
```
<br>Serialized via **SerialX for JUSS (protocol):**
```
some.package.Foo 55D 455.45 236.12F T;
``` 
## After introduction of variables in 1.1.5 and scope in 1.2.0: <br>
Serialized via **SerialX for JUSS (protocol + scope):**
```
some.package.Foo {
  val1 = 55D,
  val2 = 455.45,
  val3 = 236.12F,
  flag = T 
}
```
<br>Serialized via **SerialX for JUSS (scope only):**
```
{
  val1 = 55D,
  val2 = 455.45,
  val3 = 236.12F,
  flag = T 
}
```
<br>Maybe it is a question of formatting but JUSS with protocol will be the shortest one anyway. Because, in this case, instead of having some sort of key to the value you simply have its order (index)! 
And value's data type is specified by suffix if it is a primitive data type or simply by package name as the first argument in case of an object! Other arguments (count, order, type) are then specified by a SerializationProtocol! Generally, one line means one object, one value (separated by spaces) means one argument! <br><br>
Note: Since there is variable system in 1.1.5, the order of values is now not the only option to obtain an object or value! <br>
<br>
## Info
* If you want to add or see issues just click on [Issues section](https://github.com/PetoPetko/Java-SerialX/issues) in up.
* If you want to comment or suggest a feature use [Discussions section](https://github.com/PetoPetko/Java-SerialX/discussions).
* If you want to see or learn some things about library then see the documentation (src.zip) or [examples](https://github.com/SimplyProgrammer/Java-SerialX/tree/master/examples).
* If you want to download the library, dont use commits section, use [Releases section](https://github.com/PetoPetko/Java-SerialX/releases) or click that big green button "Clone or download" to download the latest version.
* And if you want to see changelog open [changelog file](Changelog.md) or use [Releases section](https://github.com/PetoPetko/Java-SerialX/releases) too.
