# Java-SerialX
SerialX is a powerful utility library to serialize objects in Java. Serialization means storing Java objects and values into file. <br>
SerialX is improving regular Java Base64 serialization and adding serialization protocols that you can create for objects that cant be serialized using regular way. For example final non-serializable objects, 3rd party objects and others. SerialX is also JSON like "programming" language (data storage) that are objects serialized into. So this allows you to serialize multiple objects into one string or also into file. But unlike to JSON, SerialX is based on determinate order of arguments and values we can say. In other words SerialX allows you to serialize **anything**, it's pretty simple to use and practically limitless, however you need to know that "order" is your friend, not an enemy.
## Brief overview of working concept and advantages compared to regular serialization:
**Regular java serialization** is strongly based on some kind of "magic" or we can say "godly reflection" which will reflectivly read all fields of object includeing private and final ones and then interprets it as Base64 string. And during deserialization it will create an empty instance of object absolutly ignoring its constructors by using some "magic" compilator process to create it instad, and then it will violently write all serialized field again includeing private and final ones which is realy not the best aproach! Also this alows you to serialize only instances of java.io.Serializable and all field must be instances of Serializable as well which is also not the most usefull thing! <br>
Compare to this, **SerialX API** is doing everything programmatically. SerialX API uses ``SerializationProtocol``s that are registred in ``ProtocolRegistry``, each working for certain class! ``SerializationProtocol`` contains to methods, ``serialize(T object)`` and ``unserialize(Object[] args)``. ``serialize(T object)`` method obtains certain object to serialize and its job is to turn this object in to array of objects that we can then create this exact object from, such as constructor arguments! These arguments are then paste into ``Serializer`` and ``Serializer`` serialize them into mentioned SerialX format data storage. During deserialization, ``Serializer`` first takes givven data serialized in SerialX unserialize them into array of objects and this array is then paste into ``unserialize(Object[] args)`` method as argument. Job of ``unserialize(Object[] args)`` method is to create an new instance of serialized object ``T`` from givven arguments! Evrything in this function is controlled by you and you can write them by your self which gives you an absolute control! <br>
**Advantages:**
* Overcoming most of regular serialization problems such as bypassing constructor!
* Powefull and highly costomizable, you have control over stuff!
* Programmaticall, meaning you can decide how objects will be serialized and deserialized!
* Fast, SerialX is almost always far more faster than regular serialization!
* Readable, SerialX as format is pretty readable for humans and is also pretty intuitive as well so can be also written by humans!
* Data types recognision, SerialX supports all primitve datatypes from java and also objects (done with protocols) compare to Json for instance!
* Small storage requirements, as you can see belove SerialX is often times far smaller than Json not even mentioning XML!
* Quantity, SerialX can nserialize multiple objects in to one file or string!
* Very easy to use, at the begining all what you need to know is ``Serializer.SerializeTo(file, objects)`` for serializing and ``Serializer.LoadFrom(f)`` for deserializing!

## Comparison: XML (.xml) vs Json (.json) vs YAML (.yml) vs SerialX (.srlx)
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
<br>Serialized via **SerialX for SerialX (protocol):**
```
some.package.Foo 55D 455.45 236.12F T;
``` 
## After introduction od variables in 1.1.5 and scope in 1.2.0: <br>
Serialized via **SerialX for SerialX (protocol + scope):**
```
some.package.Foo {
  val1 = 55D,
  val2 = 455.45,
  val3 = 236.12F,
  flag = T 
}
```
<br>Serialized via **SerialX for SerialX (scope only):**
```
{
  val1 = 55D,
  val2 = 455.45,
  val3 = 236.12F,
  flag = T 
}
```
<br>
Maybe it is a question of formating but SerialX with protocol will be the shortest one anyway. Because, in this case, instead of having some sort of key to the value you simply have its order (index)! 
And value's data type is specified by suffix if it is a primitive data type or simply by package name as the first argument in case of an object! Other arguments (count, order, type) are then specified by a SerializationProtocol! Generally, one line means one object, one value (separated by spaces) means one argument! <br><br>
Note: Since there is variable system in 1.1.5, the order of values is now not the only option to obtain an object or value! <br>
<br>
## Info
* If you want to add or see issues just click on [Issues section](https://github.com/PetoPetko/Java-SerialX/issues) in up.
* If you want to comment or suggest an feature use [Discussions section](https://github.com/PetoPetko/Java-SerialX/discussions).
* If you want to see or learn some things about library then see the documentation or Sample Open Source Implementation.
* If you want to download library, dont use commits section, use [Releases section](https://github.com/PetoPetko/Java-SerialX/releases) or click that big green button "Clone or download" to download the latest version.
* And if you want to see changelog open [changelog file](Changelog.md) or use [Releases section](https://github.com/PetoPetko/Java-SerialX/releases) too.
