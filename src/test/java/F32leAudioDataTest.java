
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.phonon.BitUtils;
import com.jme3.phonon.F32leAudioData;
import com.jme3.system.AppSettings;

public class F32leAudioDataTest extends SimpleApplication {
    /**
     * This example will read an ogg file, convert it into a 32 bit float point raw audio file and save it in /tmp/f32.raw.
     * Then it will take the converted output and re transform it back into a Signed PCM format with a number of bytes per sample 
     * specified by the constant below. The result will be saved in /tmp/f32.pcm_" + (8 * bytes.length) + "bit.raw
     */
    final int OUTPUT2_BYTES_PER_SAMPLE = 1;
    /////////////////////////////////////////////

    public static void main(String[] args) {
        F32leAudioDataTest app = new F32leAudioDataTest();
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        try {
            System.out.println("Input file 399354__romariogrande__eastandw_mono.ogg");
            AudioData ad = assetManager.loadAudio("399354__romariogrande__eastandw_mono.ogg");
            F32leAudioData f32le = new F32leAudioData(ad);
            OutputStream fo=new FileOutputStream("/tmp/f32.raw");
            f32le.writeRaw(fo);
            fo.close();


            byte bytes[]=new byte[OUTPUT2_BYTES_PER_SAMPLE];

            String n2 = "/tmp/f32.pcm_" + (8 * bytes.length) + "bit.raw";
            fo=new BufferedOutputStream(new FileOutputStream(n2));
            ByteBuffer bbf = f32le.rewind().getData();
            
            byte f[] = new byte[4];

            while (bbf.hasRemaining()) {
                bbf.get(f);
                if (bytes.length == 2) {
                    BitUtils.cnvF32leToI16le(f, bytes);
                } else if (bytes.length == 3) {
                    BitUtils.cnvF32leToI24le(f, bytes);
                } else {
                    BitUtils.cnvF32leToI8le(f, bytes);
                }
                fo.write(bytes);            
            }
            fo.close();

            System.out.println("Output file /tmp/f32.raw");
            System.out.println("Output file "+n2);

            stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}