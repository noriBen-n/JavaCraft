package javacraft;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * 立方体1つ分の頂点データ(位置+色)をGPUに送り、描画するクラス。
 * 8頂点をインデックスバッファ(EBO)で使い回すことで、頂点データの重複を避ける。
 */
public class Cube {

    // 各頂点: (x, y, z, r, g, b) の6個1組。立方体の8つの角に、区別しやすいよう色を割り当てる。
    private static final float[] VERTICES = {
            // 位置                色
            -0.5f, -0.5f, -0.5f,  1f, 0f, 0f, // 0
             0.5f, -0.5f, -0.5f,  0f, 1f, 0f, // 1
             0.5f,  0.5f, -0.5f,  0f, 0f, 1f, // 2
            -0.5f,  0.5f, -0.5f,  1f, 1f, 0f, // 3
            -0.5f, -0.5f,  0.5f,  1f, 0f, 1f, // 4
             0.5f, -0.5f,  0.5f,  0f, 1f, 1f, // 5
             0.5f,  0.5f,  0.5f,  1f, 1f, 1f, // 6
            -0.5f,  0.5f,  0.5f,  0f, 0f, 0f, // 7
    };

    // 6面 x 2三角形 x 3頂点 = 36個。同じ頂点番号を複数の面で使い回している。
    private static final int[] INDICES = {
            0, 1, 2,  2, 3, 0, // 後面 (-Z)
            4, 5, 6,  6, 7, 4, // 前面 (+Z)
            0, 4, 7,  7, 3, 0, // 左面 (-X)
            1, 5, 6,  6, 2, 1, // 右面 (+X)
            3, 2, 6,  6, 7, 3, // 上面 (+Y)
            0, 1, 5,  5, 4, 0, // 底面 (-Y)
    };

    private final int vaoId;
    private final int vboId;
    private final int eboId;

    public Cube() {
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(VERTICES.length);
        vertexBuffer.put(VERTICES).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // EBO(Element Buffer Object): 「どの頂点番号を、どの順で結んで三角形にするか」を持つバッファ
        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(INDICES.length);
        indexBuffer.put(INDICES).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        int stride = 6 * Float.BYTES;

        // location 0: 位置(x,y,z) — 1頂点あたり6個のfloatのうち先頭3個
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        // location 1: 色(r,g,b) — 続く3個。オフセットは3個分のfloatバイト数。
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, INDICES.length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void destroy() {
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
        glDeleteVertexArrays(vaoId);
    }
}
