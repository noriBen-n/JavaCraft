package javacraft;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

/**
 * GLSL(GPU上で動く小さなプログラム)をコンパイル・リンクして管理するクラス。
 * 頂点シェーダー(座標変換)とフラグメントシェーダー(色決定)の2つをセットで扱う。
 */
public class Shader {

    private final int programId;

    public Shader(String vertexSrc, String fragmentSrc) {
        int vertexId = compile(GL_VERTEX_SHADER, vertexSrc);
        int fragmentId = compile(GL_FRAGMENT_SHADER, fragmentSrc);

        // 2つのシェーダーを1つの「プログラム」としてGPU上でリンクする
        programId = glCreateProgram();
        glAttachShader(programId, vertexId);
        glAttachShader(programId, fragmentId);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("シェーダーのリンクに失敗: " + glGetProgramInfoLog(programId));
        }

        // リンク後は個々のシェーダーオブジェクトは不要になるので破棄する
        glDeleteShader(vertexId);
        glDeleteShader(fragmentId);
    }

    private int compile(int type, String source) {
        int id = glCreateShader(type);
        glShaderSource(id, source);
        glCompileShader(id);

        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("シェーダーのコンパイルに失敗: " + glGetShaderInfoLog(id));
        }
        return id;
    }

    /** このシェーダープログラムを、以降の描画で使うものとして有効化する。 */
    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    /** GLSL側の `uniform mat4 名前` に、JOMLの4x4行列を1つ渡す。 */
    public void setUniform(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programId, name);

        // JavaのMatrix4fオブジェクトを、GPUが読める生のfloat配列(16個)に変換して渡す。
        // try-with-resourcesでスタックメモリを使い、渡し終えたらすぐ解放する。
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    public void destroy() {
        glDeleteProgram(programId);
    }
}
