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
package com.jme3.phonon.scene;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.phonon.scene.material.MaterialGenerator;
import com.jme3.phonon.scene.material.PhononMaterial;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;


/**
 * PhononMeshBuilder
 */
public class PhononMeshBuilder{
    

    private static IntBuffer createIntBuffer(Integer... data) {
        if (data == null) {
            return null;
        }
        IntBuffer buff = BufferUtils.createIntBuffer(data.length);
        buff.clear();
        for(int i:data)
            buff.put(i);
        buff.flip();
        return buff;
    }

    private static void composeMesh(Spatial sx, SpatialFilter filter,MaterialGenerator matsel, Vector3f tmp1, Vector3f tmp2,
    List<Vector3f> positions,List<Integer> indices,List<PhononMaterial> materials,List<Integer> materialIndices){
            if(filter!=null&&!filter.filter(sx)) return;
			if(sx instanceof Geometry){
                Transform worldtr=sx.getWorldTransform();
                Geometry geom=(Geometry)sx;
                VertexBuffer pbuf=geom.getMesh().getBuffer(Type.Position);
                for(int i=0;i<pbuf.getNumElements();i++){
                    tmp1.x=(float)pbuf.getElementComponent(i,0);
                    tmp1.y=(float)pbuf.getElementComponent(i,1);
                    tmp1.z=(float)pbuf.getElementComponent(i,2);
                    worldtr.transformVector(tmp1,tmp2);
                    positions.add(tmp2.clone());

                }
                
                VertexBuffer ibuf=geom.getMesh().getBuffer(Type.Index);
                
                // Some code sets the index buffer as a 3 component buffer, some sets it as 1,
                // this code will hopefully support all use cases.
                int element=0;
                int component=0;
                Integer triangle[]=new Integer[3];
                do{
                    for(int t=0;t<3;t++){
                        int index=(int)ibuf.getElementComponent(element,component);
                        triangle[t]=index;
                        indices.add(index);
                        component++;
                        if(component>=ibuf.getNumComponents()){
                            element++;
                            component=0;
                        }
                    }
                // Full triangle loaded
                PhononMaterial mat=matsel.materialFor(geom,triangle);
                int matIndex=materials.indexOf(mat);
                if(matIndex==-1)matIndex=0;            
                    materialIndices.add(matIndex);
                }while(element<ibuf.getNumElements());
              
        }else if(sx instanceof Node){
            for(Spatial child:((Node)sx).getChildren()){
                composeMesh(child,filter,matsel,tmp1,tmp2,positions,indices,materials,materialIndices);
            }
        }
    }
    
    public static PhononMesh build(Node n, SpatialFilter filter,MaterialGenerator matsel) {
        List<Vector3f> positions=new ArrayList<Vector3f>();
        List<Integer> indices=new ArrayList<Integer>();
        List<Integer> materialIndices=new ArrayList<Integer>();
        
        Vector3f tmp1=new Vector3f();
        Vector3f tmp2=new Vector3f();

        List<PhononMaterial> materials=matsel.getAllMaterials();
        composeMesh(n,filter,matsel,tmp1,tmp2,positions,indices,materials,materialIndices);
      
        
        FloatBuffer positionBuffer=BufferUtils.createFloatBuffer((Vector3f[])positions.toArray(new Vector3f[0]));
        IntBuffer indexBuffer=createIntBuffer((Integer[])indices.toArray(new Integer[0]));
        IntBuffer materialsBuffer=createIntBuffer((Integer[])materialIndices.toArray(new Integer[0]));
        int numTriangles=indexBuffer.limit()/3;
        int numVertices=positionBuffer.limit()/3;
        PhononMesh mesh=new PhononMesh(numTriangles,numVertices,positionBuffer,indexBuffer,materialsBuffer);

        return mesh;
    }
}