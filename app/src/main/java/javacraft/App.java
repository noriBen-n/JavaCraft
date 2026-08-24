package javacraft;

public class App {

    // 頂点シェーダー：各頂点の座標(gl_Position)を決める。今回は変換せずそのまま渡す。
    private static final String VERTEX_SHADER = """
            #version 330 core
            layout (location = 0) in vec3 aPos;

            void main() {
                gl_Position = vec4(aPos, 1.0);
            }
            """;

    // フラグメントシェーダー：各ピクセルの色(FragColor)を決める。今回は固定でオレンジ色。
    private static final String FRAGMENT_SHADER = """
            #version 330 core
            out vec4 FragColor;

            void main() {
                FragColor = vec4(1.0, 0.5, 0.2, 1.0);
            }
            """;

    private final Window window;
    private boolean running = true;

    private Shader shader;
    private Triangle triangle;

    public App() {
        this.window = new Window(800, 600, "JavaCraft");
    }

    public void run() {
        window.init();

        shader = new Shader(VERTEX_SHADER, FRAGMENT_SHADER);
        triangle = new Triangle();

        while (running && !window.shouldClose()) {
            window.clear();

            shader.bind();
            triangle.render();
            shader.unbind();

            window.update();
        }

        triangle.destroy();
        shader.destroy();
        window.destroy();
    }

    public static void main(String[] args) {
        new App().run();
    }
}
