/**
* Copyright (c) 2018, Riccardo Balbo - Lorenzo Catania
* All rights reserved.
*
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright
*      notice, this list of conditions and the following disclaimer.
*
* - Redistributions in binary form must reproduce the above copyright
*      notice, this list of conditions and the following disclaimer in the
*      documentation and/or other materials provided with the distribution.
*
* - Neither the name of the developers nor the
*      names of the contributors may be used to endorse or promote products
*      derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
package com.jme3.phonon;

import com.jme3.system.NativeLibraryLoader;
import com.jme3.system.Platform;

/**
 * PhononNativeLoader
 */
public class PhononNativeLoader {

    public static void loadAll() {
        NativeLibraryLoader.registerNativeLibrary("Phonon", Platform.Linux64,
            "native/Linux/x86_64/libphonon.so");
        NativeLibraryLoader.registerNativeLibrary("JMEPhonon", Platform.Linux64,
            "native/Linux/x86_64/libjmephonon.so");

        NativeLibraryLoader.registerNativeLibrary("Phonon", Platform.Windows64,
            "native/Windows/x86_64/phonon.dll");
        NativeLibraryLoader.registerNativeLibrary("JMEPhonon", Platform.Windows64,
            "native/Windows/x86_64/jmephonon.dll");

        NativeLibraryLoader.registerNativeLibrary("Phonon", Platform.MacOSX64,
            "native/OSX/x86_64/libphonon.dylib");
        NativeLibraryLoader.registerNativeLibrary("JMEPhonon", Platform.MacOSX64,
                "native/OSX/x86_64/libjmephonon.dylib");
            

        NativeLibraryLoader.loadNativeLibrary("Phonon",true);
        NativeLibraryLoader.loadNativeLibrary("JMEPhonon",true);
    }

}