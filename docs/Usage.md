# Usage

This section is WiP
_____


## Getting started

First things first, you'll need some settings

```java
JavaSoundPhononSettings settings=new JavaSoundPhononSettings();
```

there are several things you can configure, but for now we'll keep the defaults.


Next you'll need to initialize the renderer. 

The following code will take care of performing all the initializations for you, just call this in your app init callback *( in simpleAppInit if you use SimpleApplication )*.

Note: it's better if you do this at the beginning of the initialization, because it will replace some components (the audio renderer and the listener), but you can "technically" call it wherever you want. 

```java
PhononRenderer renderer=Phonon.init(settings, this /*Your application*/);
```

## Loading the static scene (optional)

Some effects ( occlusion, reverberance, propagation ) will need to know what your scene looks like, if you are planning to use any, you'll have to send your scene to phonon.

```java
Phonon.loadScene(settings, this /*Your application*/, myStaticScene /*Your Node*/,null /*null or a spatial filter*/);
```

You can either decide to build a Node with only the occluders, or use a SpatialFilter to select only the occluders from a Node (eg from the rootNode).

Note: atm occluders can only be static, if you change the scene you have to call this method again. 

To unload the scene, use null in place of myStaticScene.

## Setting the materials (optional)

Materials define how sounds interact with the surfaces.

You can define your own material with:

```java
com.jme3.phonon.scene.material.PhononMaterial(
        String name,
        float lowFreqAbsorption,
        float midFreqAbsorption,
        float highFreqAbsorption,
        float scattering,
        float lowFreqTransmission,
        float midFreqTransmission,
        float highFreqTransmission
);
```

Or use a preset from:

```java
com.jme3.phonon.scene.material.PhononMaterialPresets
```

To assign the materials to your scene, you'll need to write a class that implements 
```java
com.jme3.phonon.scene.material.MaterialGenerator
```
or if you only need a single material for the whole scene, you can use the provided `SingleMaterialGenerator`


Once you have your material generator, you need to assign it in the settings

```java
settings.materialGenerator=new SingleMaterialGenerator(PhononMaterialPresets.metal);
```





**Note: It has to be done before `Phonon.loadScene(...)`**


## Playing the sounds

If you are familiar with jmonkey's default audio system, you know that sounds are assigned to audio nodes, this is not the case in jmePhonon.

To play a sound in jmePhonon you'll need one of the following controls

- com.jme3.phonon.scene.emitters.SoundEmitterControl
   - For environmental sounds (non positional, non directional)
- com.jme3.phonon.scene.emitters.PositionalSoundEmitterControl
    - For positional sounds
- com.jme3.phonon.scene.emitters.DirectionalSoundEmitterControl
    - For directional sounds


The control needs then to be attached to the Spatial that is supposed to emit it.

For example the background music will be a SoundEmitterControl attached to the rootNode
```java

    SoundEmitterControl background=new SoundEmitterControl(assetManager,"Sounds/xyz.wav");
    rootNode.addControl(background);
    background.setLooping(true);
    background.play();

```

The character's footsteps will be a PositionalSoundEmitterControl attached to the character
```java

    PositionalSoundEmitterControl footsteps=new PositionalSoundEmitterControl(assetManager,"Sounds/xyz.wav");
    myCharacter.addControl(footsteps);
    footsteps.playInstance();
```

And so on.

AudioNodes will still be playable by jmePhonon but it won't be possible to configure most of the settings for them.

jmePhonon provides also an utility that can be used to automatically convert AudioNodes to `Node + *SoundEmitterControl`, it can be used as follows

```java
    myScene.depthFirstTraversal(sx -> {
        // Convert audio nodes to sound emitters
        if(sx instanceof AudioNode){
            AudioNodesToControl.convert(assetManager, (AudioNode)sx);
        }
    });
```


## F32le(S) format

Internally jmePhonon uses only samples encoded in 32bit floats.

While all formats supported by jme _should_ be automatically converted at runtime, we also register two assets loader that can be used to load raw audio data with samples encoded as little endian 32 bit floats with interleaved channels.

Those files must end with one of the following extensions:
 - f32le (if the sound is mono)
 - f32leS (if the sound is stereo)

To do this from audacity you need to

```
- Go to 

File -> Export Audio 

- Select 

Header: RAW (header-less)
Encoding: 32-bit float

- Save using f32le (for mono sounds) or f32leS (for stereo sounds) as file extension.
```


