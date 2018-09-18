# Usage

This section is a WiP, you can find a quick explaination below until we write proper documentation.
_____

First you'll need the settings
```java
JavaSoundPhononSettings settings=new JavaSoundPhononSettings();
```
there are several things that you can configure but default settings are usually fine.

Next you have to inizialize the renderer, this is usually done in simpleAppInit

```java
PhononRenderer renderer=Phonon.init(settings, this /*Your application*/);
```

If your application extends SimpleApplication or LegacyApplication, this is all you need to do to initialize jmePhonon, Phonon.init() will in fact automatically deinitialize and replace the internal jme's audio renderer and listener.


You might want to use spatial occlusion, to do that you can use this method
```java
Phonon.loadScene(settings, this /*Your application*/, myStaticScene /*Your Node*/,null /*null or a spatial filter*/);
```
You can either decide to build a Node with only the occluders, or use a SpatialFilter to select only the occluders from a Node (eg from the rootNode).

Occluders can only be static, if you change the scene you have to call this method again. 

To disable the spatial occlusion, replace myStaticScene with null.