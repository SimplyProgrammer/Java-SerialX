# Java-SerialX
SerialX is a powerful utility library to serialize objects in Java. Serialization means storing Java objects and values into file. <br>
SerialX is improving regular Java Base64 serialization and adding serialization protocols that you can create for objects that cant be serialized using regular way. For example final non-serializable objects, 3rd party objects and others. SerialX is also JSON like "programming" language (data storage) that are objects serialized into. So this allows you to serialize multiple objects into one string or also into file. But unlike to JSON, SerialX is based on determinate order of arguments or values we can say. In other words SerialX allows you to serialize **anything**, it's pretty simple to use and practically limitless, however you need to know that "order" is your friend, not an enemy.
## Comparison: JACKSON (Json) vs XMLEncoder (XML) vs SerialX (SerialX data storage)
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
Serialized via **Json:**
```
...
{
  "val1" : 55,
  "val2" : 455.45,
  "val3" : 236.12,
  "flag" : true 
}
```
Serialized via **XML:**
```
<?xml version="1.0" encoding="UTF-8"?>
<java version="1.8.0_92" class="java.beans.XMLDecoder">
    <object class="org.some.beautiful.Foo">
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
Serialized via **SerialX:**
```
org.some.beautiful.Foo 55D 455.45 236.12F T;
```
Maybe it is a question of formating but SerialX will be the shortest one anyway. Because instead of having some sort of key to the value you simply have its order (index)! 
And value's data type is specified by suffix if it is a primitive data type or simply by package name as the first argument in case of an object! Other arguments (count, order, type) are then specified by a SerializationProtocol! Generally, one line means one object, one value (separated by spaces) means one argument! <br>
Note: Since there is variable system in 1.5.0, the order of values is now not the only option to obtain an object or value!
<br>
## Info
* If you want to add or see issues just click on [Issues section](https://github.com/PetoPetko/Java-SerialX/issues) in up.
* If you want to comment use [Issues section](https://github.com/PetoPetko/Java-SerialX/issues) too.
* If you want to see or learn some things about library then see the documentation or Sample Open Source Implementation.
* If you want to download library, dont use commits section, use [Releases section](https://github.com/PetoPetko/Java-SerialX/releases) or click that big green button "Clone or download" to download the latest version.
* And if you want to see changelog open [changelog file](Changelog.md) or use [Releases section](https://github.com/PetoPetko/Java-SerialX/releases) too.
