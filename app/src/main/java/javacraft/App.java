package javacraft;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;

public class App {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int GROUND_SIZE = 5; // 5x5マスの地面をとりあえず作る

    private static final String VERTEX_SHADER = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            layout (location = 1) in vec3 aColor;

            uniform mat4 uMvp;

            out vec3 vColor;

            void main() {
                gl_Position = uMvp * vec4(aPos, 1.0);
                vColor = aColor;
            }
            """;

    private static final String FRAGMENT_SHADER = """
            #version 330 core
            in vec3 vColor;
            out vec4 FragColor;

            void main() {
                FragColor = vec4(vColor, 1.0);
            }
            """;

    private final Window window;
    private boolean running = true;

    private Shader shader;
    private Chunk chunk;
    private Player player;
    private ChunkMesh chunkMesh; // Chunk全体を1つにまとめたメッシュ。dirtyな時だけ作り直す。

    public App() {
        this.window = new Window(WIDTH, HEIGHT, "JavaCraft");
    }

    public void run() {
        window.init();
        window.captureCursor();
        glEnable(GL_DEPTH_TEST);

        shader = new Shader(VERTEX_SHADER, FRAGMENT_SHADER);

        chunk = new Chunk();
        for (int x = 0; x < GROUND_SIZE; x++) {
            for (int z = 0; z < GROUND_SIZE; z++) {
                chunk.setBlock(x, 0, z, BlockType.GRASS);
            }
        }

        Matrix4f projection = new Matrix4f()
                .perspective((float) Math.toRadians(60.0), (float) WIDTH / HEIGHT, 0.1f, 100.0f);

        // 地面(5x5)の上、少し高い位置からスタートする。重力で落下して着地するのが見えるはず。
        player = new Player(new Vector3f(2.0f, 5.0f, 2.0f));

        double lastTime = window.getTime();

        while (running && !window.shouldClose()) {
            double currentTime = window.getTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            player.update(window, chunk, deltaTime);
            rebuildMeshIfDirty();

            window.clear();
            glClear(GL_DEPTH_BUFFER_BIT);

            shader.bind();
            shader.setUniform("uMvp", new Matrix4f(projection).mul(player.getCamera().getViewMatrix()));
            chunkMesh.render();
            shader.unbind();

            window.update();
        }

        chunkMesh.destroy();
        shader.destroy();
        window.destroy();
    }

    /** Blockが変更された時だけメッシュを再構築する。変更が無ければ何もしない(=軽い)。 */
    private void rebuildMeshIfDirty() {
        if (!chunk.isDirty()) {
            return;
        }
        if (chunkMesh != null) {
            chunkMesh.destroy(); // 古いGPUリソースを先に解放してから作り直す
        }
        chunkMesh = new ChunkMesh(chunk);
        chunk.clearDirty();
    }

    public static void main(String[] args) {
        new App().run();
    }
}
