
/**
 **   __ __|_  ___________________________________________________________________________  ___|__ __
 **  //    /\                                           _                                  /\    \\  
 ** //____/  \__     __ _____ _____ _____ _____ _____  | |     __ _____ _____ __        __/  \____\\ 
 **  \    \  / /  __|  |     |   __|  _  |     |  _  | | |  __|  |     |   __|  |      /\ \  /    /  
 **   \____\/_/  |  |  |  |  |  |  |     | | | |   __| | | |  |  |  |  |  |  |  |__   "  \_\/____/   
 **  /\    \     |_____|_____|_____|__|__|_|_|_|__|    | | |_____|_____|_____|_____|  _  /    /\     
 ** /  \____\                       http://jogamp.org  |_|                              /____/  \    
 ** \  /   "' _________________________________________________________________________ `"   \  /    
 **  \/____.                                                                             .____\/     
 **
 ** Utility methods dealing with shader loading, compilation, linking, verification and
 ** uniform setup. Currently vertex- and fragment-shaders are supported.
 **
 **/

import java.io.*;
import java.nio.*;
import javax.media.opengl.*;
import com.jogamp.common.nio.*;
import static javax.media.opengl.GL3bc.*;

public class ShaderUtils {

    
    //uses the correct logcalls and avoids the oldskool ARB stuff ...
    //suggested by julien: http://jogamp.762907.n3.nabble.com/problems-with-shaders-tt2092883.html#a2316436
    public static void checkShaderLogInfo(GL2 inGL, int inShaderObjectID) {
        IntBuffer tReturnValue = Buffers.newDirectIntBuffer(1);
        inGL.glGetShaderiv(inShaderObjectID, GL_COMPILE_STATUS, tReturnValue);
        if (tReturnValue.get(0) == GL.GL_FALSE) {
                inGL.glGetShaderiv(inShaderObjectID, GL_INFO_LOG_LENGTH, tReturnValue);
                final int length = tReturnValue.get(0);
                String out = null;
                if (length > 0) {
                    final ByteBuffer infoLog = Buffers.newDirectByteBuffer(length);
                    inGL.glGetShaderInfoLog(inShaderObjectID, infoLog.limit(), tReturnValue, infoLog);
                    final byte[] infoBytes = new byte[length];
                    infoLog.get(infoBytes);
                    out = new String(infoBytes);
                    System.out.print(out);
                }
                throw new GLException("Error during shader compilation: " + out);
            } 
    }
    

    public static String loadShaderSourceFileAsString(String inFileName) {
    	System.err.println("LOADING SHADER SOURCECODE FROM "+inFileName);
        try {
            //BufferedReader tBufferedReader = new BufferedReader(new InputStreamReader((new Object()).getClass().getResourceAsStream(inFileName)));
        	BufferedReader tBufferedReader = new BufferedReader(new FileReader(inFileName));
            StringBuilder tStringBuilder = new StringBuilder();
            String tCurrentLine;
            while ((tCurrentLine = tBufferedReader.readLine()) != null) {
                tStringBuilder.append(tCurrentLine);
                tStringBuilder.append("\n");
            }
            return tStringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public static int loadVertexShaderFromFile(GL2 inGL,String inShaderSourceFileName) {
        return generateVertexShader(inGL,loadShaderSourceFileAsString(inShaderSourceFileName));
    }

    public static int loadFragmentShaderFromFile(GL2 inGL,String inShaderSourceFileName) {
        return generateFragmentShader(inGL,loadShaderSourceFileAsString(inShaderSourceFileName));
    }

    public static int generateVertexShader(GL2 inGL,String inShaderSource) {
        return generateShader(inGL,inShaderSource,GL_VERTEX_SHADER);
    }

    public static int generateFragmentShader(GL2 inGL,String inShaderSource) {
        return generateShader(inGL,inShaderSource,GL_FRAGMENT_SHADER);
    }

    public static int generateShader(GL2 inGL,String inShaderSource,int inShaderType) {
        int tShader = inGL.glCreateShader(inShaderType);
        String[] tShaderSource = {inShaderSource};
        inGL.glShaderSource(tShader, 1, tShaderSource, (int[])null, 0);
        inGL.glCompileShader(tShader);
        checkShaderLogInfo(inGL, tShader);
        return tShader;
    }

    public static int generateSimple_1xVS_ShaderProgramm(GL2 inGL,int inVertexShaderObjectID) {
        return generateSimple_1xFS_OR_1xVS_ShaderProgramm(inGL,inVertexShaderObjectID);
    }

    public static int generateSimple_1xFS_ShaderProgramm(GL2 inGL, int inFragmentShaderObjectID) {
        return generateSimple_1xFS_OR_1xVS_ShaderProgramm(inGL,inFragmentShaderObjectID);
    }

    public static int generateSimple_1xFS_OR_1xVS_ShaderProgramm(GL2 inGL, int inGenericShaderObjectID) {
        int tLinkedShader = inGL.glCreateProgram();
        inGL.glAttachShader(tLinkedShader, inGenericShaderObjectID);
        inGL.glLinkProgram(tLinkedShader);
        inGL.glValidateProgram(tLinkedShader);
        checkShaderLogInfo(inGL, tLinkedShader);
        return tLinkedShader;
    }

    public static int generateSimple_1xVS_1xFS_ShaderProgramm(GL2 inGL, int inVertexShaderObjectID, int inFragmentShaderObjectID) {
        int tLinkedShader = inGL.glCreateProgram();
        inGL.glAttachShader(tLinkedShader, inVertexShaderObjectID);
        inGL.glAttachShader(tLinkedShader, inFragmentShaderObjectID);
        inGL.glLinkProgram(tLinkedShader);
        inGL.glValidateProgram(tLinkedShader);
        checkShaderLogInfo(inGL, tLinkedShader);
        return tLinkedShader;
    }

    public static void setUniform1f(GL2 inGL,int inProgramID, String inName,float inValue) {
        int tUniformLocation = inGL.glGetUniformLocation(inProgramID,inName);
        if (tUniformLocation != -1) {
            inGL.glUniform1f(tUniformLocation, inValue);
        } else {
        	System.err.println("UNIFORM COULD NOT BE FOUND! NAME="+inName);
        }
    }

    public static void setUniform2fv(GL2 inGL,int inProgramID, String inName, FloatBuffer inValues) {
        int tUniformLocation = inGL.glGetUniformLocation(inProgramID,inName);
        if (tUniformLocation != -1) {
            inGL.glUniform2fv(tUniformLocation, inValues.capacity()/2, inValues);
        } else {
        	System.err.println("UNIFORM COULD NOT BE FOUND! NAME="+inName);
        }
    }

    public static void setUniform3fv(GL2 inGL,int inProgramID, String inName, FloatBuffer inValues) {
        int tUniformLocation = inGL.glGetUniformLocation(inProgramID, inName);
        if (tUniformLocation != -1) {
            inGL.glUniform3fv(tUniformLocation, 1, inValues);
        } else {
        	System.err.println("UNIFORM COULD NOT BE FOUND! NAME="+inName);
        }
    }

    public static void setUniform4fv(GL2 inGL,int inProgramID, String inName, FloatBuffer inValues) {
        int tUniformLocation = inGL.glGetUniformLocation(inProgramID, inName);
        if (tUniformLocation != -1) {
            inGL.glUniform4fv(tUniformLocation, 1, inValues);
        } else {
            System.err.println("UNIFORM COULD NOT BE FOUND! NAME="+inName);
        }
    }

    public static void setUniform1i(GL2 inGL,int inProgramID,String inName,int inValue) {
        int tUniformLocation = inGL.glGetUniformLocation(inProgramID,inName);
        if (tUniformLocation != -1) {
            inGL.glUniform1i(tUniformLocation, inValue);
        } else {
            System.err.println("UNIFORM COULD NOT BE FOUND! NAME="+inName);
        }
    }

   /*public static void setSampler2DUniformOnTextureUnit(GL2 inGL,int inProgramID,String inSamplerUniformName,Texture inTexture,int inTextureUnit,int inTextureUnitNumber,boolean inPreferAnisotropy) {
        inGL.glActiveTexture(inTextureUnit);
        inTexture.enable();
        inTexture.bind();
        if (inPreferAnisotropy) {
            TextureUtils.preferAnisotropicFilteringOnTextureTarget(inGL,inTexture.getTarget());
        }
        ShaderUtils.setUniform1i(inGL,inProgramID,inSamplerUniformName,inTextureUnitNumber);
        inTexture.disable();
    }*/

}
