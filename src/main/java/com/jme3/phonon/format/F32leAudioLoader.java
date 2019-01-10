package com.jme3.phonon.format;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.jme3.util.BufferUtils;

/**
 * F32leAudioLoader
 */
public class F32leAudioLoader implements AssetLoader{
    protected int NCHANNELS=1;

	@Override
    public F32leAudioData load(AssetInfo assetInfo) throws IOException {
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        InputStream is=assetInfo.openStream();
        byte chunk[]=new byte[1024*64];
        int read;

        while((read=is.read(chunk))!=-1){
            bos.write(chunk,0,read);
        }
        is.close();
        byte data[]=bos.toByteArray();
        bos.close();
        ByteBuffer bbf=BufferUtils.createByteBuffer(data.length);
        bbf.put(data);
        bbf.rewind();
        F32leAudioData f32le=new F32leAudioData(NCHANNELS,44100,bbf);
        f32le.rewind();
        
		return f32le;
	}

    
}