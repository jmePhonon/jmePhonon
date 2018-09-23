
# Contribution and Development
_________
**I don't have time for this, just tell me how to do stuff**: Ok, jump to [#TL;DR](#tldr)
_______

## The BuildSystem: Docker

To facilitate the building of this project we ship the entire build environment as a docker image, you'll only need a working installation of docker and an user with enough privileges to run it.

*Note: the docker container will be automatically deleted after every build*

### How to install and configure docker
- Linux: 
     [Ubuntu](https://docs.docker.com/install/linux/docker-ce/ubuntu/)
    , [CentOS](https://docs.docker.com/install/linux/docker-ce/centos/)
    , [Debian](https://docs.docker.com/install/linux/docker-ce/debian/)
    , [Fedora](https://docs.docker.com/install/linux/docker-ce/fedora/)
    , [Others](https://docs.docker.com/install/linux/docker-ce/binaries/)
- Windows 10 Pro or Enterprise
    - Download and install docker for windows: https://docs.docker.com/docker-for-windows/install/
    - Share the driver where jmePhonon's folder is located https://docs.docker.com/docker-for-windows/#shared-drives
    - Install git bash https://gitforwindows.org/
- Windows 10 Home or 8.1, 8 or 7
    - Install docker toolbox: https://docs.docker.com/toolbox/toolbox_install_windows/#what-you-get-and-how-it-works
- MacOS: https://docs.docker.com/docker-for-mac/install/

       


## The BuildSystem: `make.sh` / `make.bat`


The build is triggered by either `make.sh` (on linux and macos) or `make.bat` on windows.

The make script will take care of everything, from building the docker image (the first run might be slower because of this), to running and removing the containers.

A quick overview of the usage of make.sh will follow, don't worry we'll see everything in details in the next sections.

**Note: when running on windows, replace `make.sh` with `make.bat`**


```bash
./make.sh ENV_VAR task1 [task2] [task3] ...

    ENV_VAR :  csv list of environment variables to pass to the build environment
    task1 [task2] [task4] : one or more build tasks

Example:
    ./make.sh DEBUG=1,OS_WINDOWS=1 buildNatives
    Build unoptimized debug binaries for windows.
```
*Note: files deleted by the build script will be moved into a .Trash folder located on the root of the project, you can use the task **deepClean** or empty it by hand*

### Environment Variables
- **OS_LINUX=1** *build for linux*
- **OS_WINDOWS=1** *build for windows*
- **OS_OSX=1** *build for osx*
- **DEBUG=1** *build with debug symbols*
- **NO_CACHE=1** *do not preserve caches across builds*
- **TRASH=path** *custom path for trash*
- **TRASH=notrash** *don't use trash, rm is for ever*

*Note: you can specify multiple OS targets like this*
```bash
./make.sh OS_LINUX=1,OS_WINDOWS=1,OS_OSX=1 buildNatives
```

### Build tasks
- **updateJNIHeaders** *generates updated .h headers*
- **buildNative** *builds native binaries*
- **build** *builds artifact*
- **deepClean** *removes every temporary file and empties the local trash*
- **clean** *gradle's clean*
- **prepareWorkspace** *downloads needed dependencies and generates a visual studio code/eclipse compatible workspace*
- **downloadResources** downloads test resources
- **deploy** moves releases into a deploy folder


### I don't like docker/I'm not afraid of dependencies/Install Gentoo

You can still run the build tasks without using docker by replacing `make.sh` with `gradle`. eg.
```bash 
OS_LINUX=1 NO_CACHE=1 gradle buildNative
```

*Note: multi target is not supported, you'll have to call the command for every os for which you intend to build. eg.*
```bash
OS_LINUX=1 NO_CACHE=1 gradle buildNative
OS_WINDOWS=1 NO_CACHE=1 gradle buildNative
```
*This means that*
```bash
OS_LINUX=1 OS_WINDOWS=1 gradle buildNatives
```
*will not work, while it will if you use make.sh.*

*Note also that you'll have to manually install all the required dependencies and a bash compatible environment for Windows < 10*


## Preparation

### 1. Clone the repo
The first step is to clone the repo, this command will also tell git to skip `git lfs pull`, if needed you can download the test resources later using a build task.

```bash
GIT_LFS_SKIP_SMUDGE=1 git clone https://github.com/jmePhonon/jmePhonon.git
```

### 2. Prepare the workspace
The next thing we need to do is fetching Steam® Audio SDK, the machine dependants jni headers, and make them accessible by the build script, don't worry we have a task that does just that:
```bash
./make.sh generic prepareWorkspace eclipse
```

### 2.1 (Optional). Test resources
You might also want some testing files to try the code, we've got you covered
```bash
./make.sh generic downloadResources
``` 

## Building
### The environment variables
You'll have to provide a list of environment variables to configure the following tasks, you can jump back to [#Environment Variables](#environment-variables) to see what is available and what it does.
For this doc we'll use the following list
```bash
DEBUG=1,OS_LINUX=1,OS_WINDOWS=1,OS_OSX=1
```
that generates debug builds for windows linux and osx, if you intend to build a release (optimized, w/o debug symbols) just remove `DEBUG=1,` from the list


### Update JNI headers
Every time you change/add/remove (java) native methods you need to regenerate the jni headers.

The task to do that is
```bash
./make.sh DEBUG=1,OS_LINUX=1,OS_WINDOWS=1,OS_OSX=1 updateJNIHeader -xbuildNatives
```

### Build the natives
When you make changes to the C bindings you have to recompile the natives, the task is
```bash
./make.sh DEBUG=1,OS_LINUX=1,OS_WINDOWS=1,OS_OSX=1 buildNatives
```


### Build the artifact
The final step is to get something that you can actually include in other projects
```bash
./make.sh DEBUG=1,OS_LINUX=1,OS_WINDOWS=1,OS_OSX=1 build deploy
```
This tasks will do just that, you'll get a jar artifacts in projectRoot/deploy that contains the java classes and the binaries needed to use jmePhonon on your own project.

### Build runnable tests
You might want to run the tests on different machines, since moving workspaces around is not always comfy, we have a task that builds a portable jar that contains the tests (that you can run with java -cp testjar.jar tests.Test1234):

```bash
./make.sh DEBUG=1,OS_LINUX=1,OS_WINDOWS=1,OS_OSX=1 buildStandaloneTests deploy
```

### Things have gone bad, my workspace is broken and i don't know what to do
np, run this
```bash
./make.sh generic deepClean
```
or this
```bash
gradle deepClean
```
Every temporary file will be removed. 

_Note that after this task is completed, you'll have to rebuild the workspace, jump back to [#Preparation](#preparation) to see how._


## The Documentation

Steam® Audio very useful official documentation is here https://valvesoftware.github.io/steam-audio/doc/capi/index.html be sure to read also the content of `src/steamaudio/phonon.h`, it might be useful to get a less dispersive overview .

jmePhonon documentation is still WiP.

## Contribution
> I want to implement something new.

Checkout the [TODO list](https://github.com/jmePhonon/jmePhonon/issues?q=is%3Aissue+is%3Aopen+label%3A%22TODO+%3Acrab%3A%22)

> I want to improve something

Checkout the [Improvements list](https://github.com/jmePhonon/jmePhonon/issues?q=is%3Aissue+is%3Aopen+label%3A%22Need+improvements%22)

> I want to fix something that is broken

Checkout the [BUG list](https://github.com/jmePhonon/jmePhonon/issues?q=is%3Aopen+is%3Aissue+label%3A%22bug+%3Abug%3A%22)

> I am the inspector gadget

Checkout the [Investigation list](https://github.com/jmePhonon/jmePhonon/issues?q=is%3Aissue+is%3Aopen+label%3A%22Need+investigation+%3Amale_detective%3A%22)

> I just want to report an issue / make a suggestion

Please do it: [Issue tracker](https://github.com/jmePhonon/jmePhonon/issues)

> I want to do something else.

Just do it! Feel free to use the [Issue tracker](https://github.com/jmePhonon/jmePhonon/issues) to discuss about it.

> I've already done it

Nice, feel free to [submit a PR ](https://github.com/jmePhonon/jmePhonon/pulls)

## TL;DR 

### THINGS YOU NEED
1. DOCKER
2. USER CAPABLE OF RUNNING DOCKER

NOTE: REPLACE `make.sh` WITH `make.bat` WHEN RUNNING ON WINDOWS.

###  PREPARE WORKSPACE (RUN ONLY ONCE):
```bash
./make.sh OS_LINUX=1,OS_WINDOWS=1,OS_OSX=1 prepareWorkspace downloadResources eclipse
```
###  BUILD STUFF:
```bash
./make.sh OS_LINUX=1,OS_WINDOWS=1,OS_OSX=1 updateJNIHeader buildNatives build deploy
```
###  HELP! EVERYTHING IS MESSEDUP IDK WHAT TO DO ANYMORE:
```
./make.sh OS_LINUX=1,OS_WINDOWS=1,OS_OSX=1 deepClean
./make.sh OS_LINUX=1,OS_WINDOWS=1,OS_OSX=1 prepareWorkspace downloadResources eclipse
```
### HELP! IT SAYS SOMETHING ABOUT ME NOT HAVING PERMISSIONS TO RUN DOCKER AND STUFF

ADD YOURSELF TO THE DOCKER GROUP. OR USE SUDO.

_Note: don't `sudo su` and then `./make.sh`, but rather use `sudo ./make.sh`._

### DOCS
Steam® Audio: https://valvesoftware.github.io/steam-audio/doc/capi/index.html

jmePhonon: TODO
