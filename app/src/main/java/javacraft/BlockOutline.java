package javacraft;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * 狙っているBlockを線(ワイヤーフレーム)で縁取って表示する。
 * 形状は1つだけ作って使い回し、毎フレーム対象Blockの座標へMVP行列で移動させて描画する。
 */
public class BlockOutline {

    // 立方体の8頂点。ChunkMeshと同じ「座標(x,y,z)は(x,y,z)〜(x+1,y+1,z+1)を占める」座標系に合わせる。
    // 少しだけ外側に膨らませて(-M〜1+M)、Block本体の面とのチラつき(Zファイティング)を防ぐ。
    private static final float M = 0.01f;
    private static final float LOW = -M;
    private static final float HIGH = 1f + M;

    private static final float[] VERTICES = {
            // 位置(x,y,z)               色(白、目立たせるため)
            LOW,  LOW,  LOW,   1f, 1f, 1f, // 0
            HIGH, LOW,  LOW,   1f, 1f, 1f, // 1
            HIGH, HIGH, LOW,   1f, 1f, 1f, // 2
            LOW,  HIGH, LOW,   1f, 1f, 1f, // 3
            LOW,  LOW,  HIGH,  1f, 1f, 1f, // 4
            HIGH, LOW,  HIGH,  1f, 1f, 1f, // 5
            HIGH, HIGH, HIGH,  1f, 1f, 1f, // 6
            LOW,  HIGH, HIGH,  1f, 1f, 1f, // 7
    };

    // 12本の辺 = 24個の頂点番号(2個1組)。GL_LINESで描画する。
    private static final int[] EDGES = {
            0, 1, 1, 2, 2, 3, 3, 0, // 後面の4辺
            4, 5, 5, 6, 6, 7, 7, 4, // 前面の4辺
            0, 4, 1, 5, 2, 6, 3, 7, // 前後を繋ぐ4辺
    };

    private final int vaoId;
    private final int vboId;
    private final int eboId;

    public BlockOutline() {
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(VERTICES.length);
        vertexBuffer.put(VERTICES).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(EDGES.length);
        indexBuffer.put(EDGES).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        int stride = 6 * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawElements(GL_LINES, EDGES.length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void destroy() {
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
        glDeleteVertexArrays(vaoId);
    }
}
