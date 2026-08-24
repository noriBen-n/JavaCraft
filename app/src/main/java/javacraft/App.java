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
    private World world;
    private Cube grassCube; // GRASSブロック用のメッシュ。全GRASSブロックで使い回す。
    private Player player;

    public App() {
        this.window = new Window(WIDTH, HEIGHT, "JavaCraft");
    }

    public void run() {
        window.init();
        window.captureCursor();
        glEnable(GL_DEPTH_TEST);

        shader = new Shader(VERTEX_SHADER, FRAGMENT_SHADER);
        grassCube = new Cube(BlockType.GRASS);

        world = new World();
        for (int x = 0; x < GROUND_SIZE; x++) {
            for (int z = 0; z < GROUND_SIZE; z++) {
                world.setBlock(x, 0, z, BlockType.GRASS);
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

            player.update(window, world, deltaTime);

            window.clear();
            glClear(GL_DEPTH_BUFFER_BIT);

            shader.bind();
            renderWorld(projection, player.getCamera().getViewMatrix());
            shader.unbind();

            window.update();
        }

        grassCube.destroy();
        shader.destroy();
        window.destroy();
    }

    private void renderWorld(Matrix4f projection, Matrix4f view) {
        for (int x = 0; x < World.SIZE; x++) {
            for (int y = 0; y < World.SIZE; y++) {
                for (int z = 0; z < World.SIZE; z++) {
                    BlockType type = world.getBlock(x, y, z);
                    if (type == BlockType.AIR) {
                        continue; // 空気ブロックは描画しない
                    }

                    // Model行列: このBlockの座標(x, y, z)へ平行移動するだけ
                    Matrix4f model = new Matrix4f().translate(x, y, z);
                    Matrix4f mvp = new Matrix4f(projection).mul(view).mul(model);

                    shader.setUniform("uMvp", mvp);
                    grassCube.render();
                }
            }
        }
    }

    public static void main(String[] args) {
        new App().run();
    }
}
