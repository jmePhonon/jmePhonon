package tests;

import java.util.Arrays;
import java.util.List;

import com.jme3.phonon.PhononSoundDevice;
import com.jme3.phonon.desktop_javasound.JavaSoundSystem;

/**
 * ListAvailableJavaSoundDevicesAndFormats
 */
public class ListAvailableJavaSoundDevicesAndFormats {

    public static void main(String[] args) {
        JavaSoundSystem system=new JavaSoundSystem();
        List<PhononSoundDevice> devices=system.getAudioDevices();
        for(PhononSoundDevice device:devices){
            System.out.println("Device "+device);
            List<Integer> formats=system.getOutputFormats(device,2,true);
               
            System.out.println(" Usable formats:"+Arrays.deepToString(formats.toArray(new Integer[0])));
        }
    }
}