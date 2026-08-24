package javacraft;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * 立方体1つ分の頂点データ(位置+色)をGPUに送り、描画するクラス。
 * 8頂点をインデックスバッファ(EBO)で使い回すことで、頂点データの重複を避ける。
 * 色はBlockTypeから決まり、8頂点すべてに同じ色を割り当てる。
 */
public class Cube {

    // 立方体の8つの角の位置(x, y, z)。色は含まない。
    private static final float[] POSITIONS = {
            -0.5f, -0.5f, -0.5f, // 0
             0.5f, -0.5f, -0.5f, // 1
             0.5f,  0.5f, -0.5f, // 2
            -0.5f,  0.5f, -0.5f, // 3
            -0.5f, -0.5f,  0.5f, // 4
             0.5f, -0.5f,  0.5f, // 5
             0.5f,  0.5f,  0.5f, // 6
            -0.5f,  0.5f,  0.5f, // 7
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

    public Cube(BlockType blockType) {
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        float[] vertexData = buildVertexData(blockType.getColor());

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexData.length);
        vertexBuffer.put(vertexData).flip();
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

    /** POSITIONS(x,y,z)の各頂点に、同じ色(r,g,b)を1組にして展開する。 */
    private static float[] buildVertexData(Vector3f color) {
        int vertexCount = POSITIONS.length / 3;
        float[] data = new float[vertexCount * 6];

        for (int v = 0; v < vertexCount; v++) {
            data[v * 6]     = POSITIONS[v * 3];
            data[v * 6 + 1] = POSITIONS[v * 3 + 1];
            data[v * 6 + 2] = POSITIONS[v * 3 + 2];
            data[v * 6 + 3] = color.x;
            data[v * 6 + 4] = color.y;
            data[v * 6 + 5] = color.z;
        }
        return data;
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
