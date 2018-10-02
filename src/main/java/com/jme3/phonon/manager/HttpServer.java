package com.jme3.phonon.manager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * HttpServer
 */
public class HttpServer extends NanoHTTPD{
    public final AudioManager MNG; 
    public HttpServer(int port,AudioManager mng) throws IOException {
        super(port);
        MNG=mng;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:"+port+"/ \n");
    }
    @Override
    public Response serve(IHTTPSession session) {

        String uri=session.getUri();
        switch(uri){
            case "/get_sources":{
                return newFixedLengthResponse(MNG.getDef());
            }
            case "/source_action":{
                try{
                    final HashMap<String, String> map = new HashMap<String, String>();
                    session.parseBody(map);

                    String postBody = map.get("postData");


                    MNG.sourceAction(postBody);
                    return newFixedLengthResponse("{\"status\":\"ok\",\"desc\":\"\"}");
                 }catch(Exception re){
                    re.printStackTrace();
                    return newFixedLengthResponse("{\"status\":\"error\",\"desc\":\"\"}");
                }
            }
            case "/update_sources":{
                // try {
                 
                // }catch(Exception re){
                //     re.printStackTrace();
                //     return newFixedLengthResponse("{\"status\":\"error\",\"desc\":\"\"}");
                // }
                try{
                    final HashMap<String, String> map = new HashMap<String, String>();
                    session.parseBody(map);

                    String postBody = map.get("postData");


                    // session.parseBody(new HashMap<String, String>());
                    // ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    // InputStream is=session.getInputStream();
                    // byte chunk[]=new byte[1024*1024];
                    // int read;
                    // while((read=is.read(chunk))!=-1){
                    //     baos.write(chunk,0,read);
                    // }

                    // String postBody=new String(baos.toByteArray(),Charset.forName("UTF-8"));

                    // is.close();
                    // System.out.println(postBody);

                    MNG.updateDef(postBody);
                    return newFixedLengthResponse("{\"status\":\"ok\",\"desc\":\"\"}");
                 }catch(Exception re){
                    re.printStackTrace();
                    return newFixedLengthResponse("{\"status\":\"error\",\"desc\":\"\"}");
                }
            }
            case "/systeminfo":{
                return newFixedLengthResponse(MNG.getSysInfo());
            }

        }
       
        return newFixedLengthResponse("{\"status\":\"error\",\"desc\":\"404\"}");
    }
}