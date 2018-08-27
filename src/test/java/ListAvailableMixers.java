import java.util.*;
import javax.sound.sampled.*;

public class ListAvailableMixers {
    public ListAvailableMixers() {
    }

    public static void main(String[] args) throws Exception {
        showMixers();
    }

    public static int countLines(Mixer mixer, Line.Info lineInfo) {
        int maxLines = 0;
        ArrayList<Line> opened = new ArrayList<Line>();
        try {
            for (int i = 0; i < 1024; i++) {
                Line l = mixer.getLine(lineInfo);
                l.open();
                opened.add(l);
                maxLines++;
            }
        } catch (Exception e) {

        }
        for (Line l : opened)
            l.close();
        return maxLines;
    }

    public static void showMixers() {
        Mixer.Info mixInfos[]=AudioSystem.getMixerInfo();
        Line.Info sourceDLInfo = new Line.Info(SourceDataLine.class);

        for (Mixer.Info mixInfo : mixInfos) {
            Mixer mixer = AudioSystem.getMixer(mixInfo);
            int lines = countLines(mixer, sourceDLInfo);
            System.out.println(
                    "Mixer: " + mixInfo.getName() + " " + lines + " lines supported, " + mixInfo.getDescription());
        }
    }
}