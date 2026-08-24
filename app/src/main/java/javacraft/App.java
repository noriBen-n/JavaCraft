package javacraft;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

public class App {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    // 頂点シェーダー：位置(aPos)と色(aColor)を受け取り、MVP行列を掛けて画面上の座標を決める。
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
    private Cube cube;

    public App() {
        this.window = new Window(WIDTH, HEIGHT, "JavaCraft");
    }

    public void run() {
        window.init();
        glEnable(GL_DEPTH_TEST); // 奥行きを正しく判定するため、深度テストを有効化する

        shader = new Shader(VERTEX_SHADER, FRAGMENT_SHADER);
        cube = new Cube(BlockType.GRASS);

        // Projection行列: 遠近感(パースペクティブ)を決める。視野角・アスペクト比・近い/遠いクリップ面。
        Matrix4f projection = new Matrix4f()
                .perspective((float) Math.toRadians(60.0), (float) WIDTH / HEIGHT, 0.1f, 100.0f);

        // View行列: カメラの位置と向き。今はまだ動かないカメラを、少し引いた位置に固定で置く。
        Matrix4f view = new Matrix4f()
                .lookAt(2.0f, 2.0f, 3.0f,   // カメラの位置
                        0.0f, 0.0f, 0.0f,   // 見る対象(原点)
                        0.0f, 1.0f, 0.0f);  // 「上」方向

        float angle = 0.0f;

        while (running && !window.shouldClose()) {
            window.clear();
            glClear(GL_DEPTH_BUFFER_BIT);

            // Model行列: Cube自身の回転。3D空間で立方体だとわかりやすいよう毎フレーム少し回す。
            angle += 0.5f;
            Matrix4f model = new Matrix4f().rotateY((float) Math.toRadians(angle));

            // MVP = Projection * View * Model。頂点にはこの順で右から掛かる。
            Matrix4f mvp = new Matrix4f(projection).mul(view).mul(model);

            shader.bind();
            shader.setUniform("uMvp", mvp);
            cube.render();
            shader.unbind();

            window.update();
        }

        cube.destroy();
        shader.destroy();
        window.destroy();
    }

    public static void main(String[] args) {
        new App().run();
    }
}
