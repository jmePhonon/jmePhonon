package com.jme3.phonon.manager;

import java.util.Map;

/**
 * JSON
 */
public interface JSON {
    public String stringify(Map map);

    public Map parse(String json);
    
}