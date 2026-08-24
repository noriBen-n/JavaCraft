package javacraft;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * 三角形1つ分の頂点データをGPUに送り、描画するクラス。
 * 将来Blockの表示などに置き換わっていく、今回限りの練習用クラス。
 */
public class Triangle {

    // NDC(正規化デバイス座標)。X,Yともに-1.0〜1.0の範囲が画面全体に対応する。
    private static final float[] VERTICES = {
             0.0f,  0.5f, 0.0f,  // 上
            -0.5f, -0.5f, 0.0f,  // 左下
             0.5f, -0.5f, 0.0f,  // 右下
    };

    private final int vaoId;
    private final int vboId;

    public Triangle() {
        // VAO(Vertex Array Object): 「頂点データの読み方の設定」をまとめて記憶しておく箱
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // VBO(Vertex Buffer Object): 実際の頂点データそのものをGPUメモリ上に確保する
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(VERTICES.length);
        buffer.put(VERTICES).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        // 「このVBOの中身は、3個1組(x,y,z)のfloatとして解釈してください」とGPUに教える
        int positionAttributeIndex = 0;
        glVertexAttribPointer(positionAttributeIndex, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(positionAttributeIndex);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);
    }

    public void destroy() {
        glDeleteBuffers(vboId);
        glDeleteVertexArrays(vaoId);
    }
}
