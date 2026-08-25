package javacraft;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Chunk全体を「見える面だけ」の1つの巨大なメッシュにまとめ、1回のドローコールで描画する。
 * これまでの Cube を1個ずつ描画する方式(Block数だけドローコールが必要)を置き換える。
 */
public class ChunkMesh {

    // 立方体の8頂点(ローカル座標、中心が原点)。旧Cubeクラスと同じ形状。
    private static final Vector3f[] CORNERS = {
            new Vector3f(-0.5f, -0.5f, -0.5f), // 0
            new Vector3f(0.5f, -0.5f, -0.5f),  // 1
            new Vector3f(0.5f, 0.5f, -0.5f),   // 2
            new Vector3f(-0.5f, 0.5f, -0.5f),  // 3
            new Vector3f(-0.5f, -0.5f, 0.5f),  // 4
            new Vector3f(0.5f, -0.5f, 0.5f),   // 5
            new Vector3f(0.5f, 0.5f, 0.5f),    // 6
            new Vector3f(-0.5f, 0.5f, 0.5f),   // 7
    };

    // 面ごとに「隣を確認する方向(dx,dy,dz)」と「使う4頂点」を定義する
    private record Face(int dx, int dy, int dz, int[] cornerIndices) {}

    private static final Face[] FACES = {
            new Face(0, 0, -1, new int[]{0, 1, 2, 3}), // 後面 (-Z)
            new Face(0, 0, 1, new int[]{4, 5, 6, 7}),  // 前面 (+Z)
            new Face(-1, 0, 0, new int[]{0, 4, 7, 3}), // 左面 (-X)
            new Face(1, 0, 0, new int[]{1, 5, 6, 2}),  // 右面 (+X)
            new Face(0, 1, 0, new int[]{3, 2, 6, 7}),  // 上面 (+Y)
            new Face(0, -1, 0, new int[]{0, 1, 5, 4}), // 底面 (-Y)
    };

    private final int vaoId;
    private final int vboId;
    private final int eboId;
    private final int indexCount;

    public ChunkMesh(Chunk chunk) {
        List<Float> vertexData = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int y = 0; y < Chunk.SIZE; y++) {
                for (int z = 0; z < Chunk.SIZE; z++) {
                    BlockType type = chunk.getBlock(x, y, z);
                    if (type != BlockType.AIR) {
                        addVisibleFaces(chunk, x, y, z, type, vertexData, indices);
                    }
                }
            }
        }

        indexCount = indices.size();

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexData.size());
        for (float value : vertexData) {
            vertexBuffer.put(value);
        }
        vertexBuffer.flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.size());
        for (int value : indices) {
            indexBuffer.put(value);
        }
        indexBuffer.flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        int stride = 6 * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    /** 1つのBlockについて、隣がSolidでない面だけを頂点/インデックスのリストに追加する(Face Culling)。 */
    private void addVisibleFaces(Chunk chunk, int x, int y, int z, BlockType type,
                                  List<Float> vertexData, List<Integer> indices) {
        Vector3f color = type.getColor();

        for (Face face : FACES) {
            if (chunk.isSolid(x + face.dx(), y + face.dy(), z + face.dz())) {
                continue; // 隣が固体 = この面は絶対に見えないので生成しない
            }

            int baseIndex = vertexData.size() / 6; // 6個のfloatで1頂点なので、頂点番号に変換する

            for (int cornerIndex : face.cornerIndices()) {
                Vector3f corner = CORNERS[cornerIndex];
                // Chunk内のローカル座標(x,y,z)を、頂点データにそのまま焼き込む(世界座標が絶対値で入る)
                vertexData.add(corner.x + x);
                vertexData.add(corner.y + y);
                vertexData.add(corner.z + z);
                vertexData.add(color.x);
                vertexData.add(color.y);
                vertexData.add(color.z);
            }

            indices.add(baseIndex);
            indices.add(baseIndex + 1);
            indices.add(baseIndex + 2);
            indices.add(baseIndex + 2);
            indices.add(baseIndex + 3);
            indices.add(baseIndex);
        }
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void destroy() {
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
        glDeleteVertexArrays(vaoId);
    }
}
