package javacraft;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * 画面中央に「+」の照準を表示する。3D空間の物ではなく、常に画面座標(NDC)の中心に固定される。
 * MVPには常に単位行列(何も変換しない)を渡して描画する。
 */
public class Crosshair {

    private static final float SIZE = 0.02f;  // 腕の長さ(NDC座標系、画面全体が-1.0〜1.0)
    private static final float THICKNESS = 0.003f;

    // 横棒と縦棒、それぞれ細長い四角形として頂点を作る(色は白)
    private static final float[] VERTICES = {
            // 横棒
            -SIZE, -THICKNESS, 0f,  1f, 1f, 1f,
             SIZE, -THICKNESS, 0f,  1f, 1f, 1f,
             SIZE,  THICKNESS, 0f,  1f, 1f, 1f,
            -SIZE,  THICKNESS, 0f,  1f, 1f, 1f,
            // 縦棒
            -THICKNESS, -SIZE, 0f,  1f, 1f, 1f,
             THICKNESS, -SIZE, 0f,  1f, 1f, 1f,
             THICKNESS,  SIZE, 0f,  1f, 1f, 1f,
            -THICKNESS,  SIZE, 0f,  1f, 1f, 1f,
    };

    private static final int[] INDICES = {
            0, 1, 2, 2, 3, 0, // 横棒
            4, 5, 6, 6, 7, 4, // 縦棒
    };

    private final int vaoId;
    private final int vboId;
    private final int eboId;

    public Crosshair() {
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(VERTICES.length);
        vertexBuffer.put(VERTICES).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        java.nio.IntBuffer indexBuffer = BufferUtils.createIntBuffer(INDICES.length);
        indexBuffer.put(INDICES).flip();
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
        glDrawElements(GL_TRIANGLES, INDICES.length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void destroy() {
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
        glDeleteVertexArrays(vaoId);
    }
}
