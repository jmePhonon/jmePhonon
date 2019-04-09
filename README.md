# **jmePhonon** : _Steam® Audio for jMonkeyEngine_
<img align=right width="300px" src="https://media.githubusercontent.com/media/jmePhonon/jmePhonon/master/misc/logo.png" />

[Steam® Audio](https://valvesoftware.github.io/steam-audio/) (phonon) implementation for [jMonkeyEngine](http://jmonkeyengine.org/).

## Supported platforms
- Linux 64bit
- OSX (64bit)
- Windows 64bit


## Binaries
On [Bintray](https://bintray.com/jmephonon/jmePhonon/jmePhonon)
```gradle
repositories { 
    maven { url "https://dl.bintray.com/jmephonon/jmePhonon" }
}
dependencies{
    compile(group: 'com.jme3.phonon', name: 'jmePhonon', version: VERSION, ext: 'jar', classifier: '')
}
```

## Documentation

- I want to use jmePhonon
  - [Usage guide](docs/Usage.md)
- I want to develop jmePhonon
  - [Contribution & Development (how to build stuff)](docs/ContributionAndDevelopment.md)
  - [Implementation details](docs/ImplementationDetails.md)
  - [Javadoc](todo)


## License
The code in this repo is released under the BSD-3-Clause license, see [LICENSE.md](LICENSE.md) for more details.

## Legal note
Please ensure that you have read and understood [LICENSE.md](LICENDE.md) and expecially  [legal/LICENSE.SteamAudio.md](legal/LICENSE.SteamAudio.md).

This code uses Steam® Audio SDK, that is a closed source library licensed under the [VALVE CORPORATION STEAM® AUDIO SDK LICENSE](legal/LICENSE.SteamAudio.md).

_Steam® is a trademark or registered trademark of Valve Corporation in the United States of America and elsewhere_

_Steam® Audio, Copyright 2017 – 2018, Valve Corp. All rights reserved._
