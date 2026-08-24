package javacraft;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * OSのウィンドウ生成・破棄・入力イベントの受け取りを担当するクラス。
 * GLFW(ネイティブライブラリ)を直接扱うのはこのクラスだけに閉じ込める。
 */
public class Window {

    private final int width;
    private final int height;
    private final String title;

    // GLFWのウィンドウは「ハンドル」と呼ばれる long型の数値(実体はネイティブ側のポインタ)で扱う
    private long handle;

    public Window(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
    }

    /** GLFWの初期化とウィンドウの生成を行う。 */
    public void init() {
        // GLFWがエラーを検出した際、標準エラー出力に出すよう設定する
        GLFWErrorCallback.createPrint(System.err).set();

        // GLFWライブラリ自体の初期化。失敗したら続行不可能なので例外を投げる。
        if (!glfwInit()) {
            throw new IllegalStateException("GLFWの初期化に失敗しました");
        }

        // ウィンドウ生成前のヒント(設定)をリセットしてから指定する
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);   // 生成直後はまだ非表示にしておく
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);  // サイズ変更を許可する

        // ウィンドウを実際に生成する。失敗するとNULL(0)が返る。
        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) {
            throw new RuntimeException("GLFWウィンドウの生成に失敗しました");
        }

        // このウィンドウのOpenGL描画コンテキストを、今のスレッドで使う対象として設定する
        glfwMakeContextCurrent(handle);

        // 垂直同期(VSync)を有効化。1 = 画面のリフレッシュレートに合わせて描画を待つ。
        glfwSwapInterval(1);

        // ウィンドウを画面に表示する
        glfwShowWindow(handle);

        // LWJGLがOpenGLの関数群を呼び出せるようにする(このタイミングまで呼べない)
        GL.createCapabilities();

        // 背景を塗りつぶす色を設定しておく(まだ何も描画していないので単色になる)
        glClearColor(0.2f, 0.3f, 0.4f, 1.0f);
    }

    /** ウィンドウの×ボタン等が押されて閉じるべき状態になったかを返す。 */
    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    /** 指定したキー(例: GLFW_KEY_W)が今押されているかどうか。 */
    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(handle, keyCode) == GLFW_PRESS;
    }

    /** アプリ起動からの経過秒数。フレームレートに依存しない移動量の計算に使う。 */
    public double getTime() {
        return glfwGetTime();
    }

    /** 1フレーム分の描画準備として、前フレームの内容をクリアする。 */
    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT);
    }

    /** 描画結果を画面に反映し、OS側のイベント(キー入力、×ボタン等)を処理する。 */
    public void update() {
        glfwSwapBuffers(handle);
        glfwPollEvents();
    }

    /** ウィンドウとGLFWのリソースを解放する。 */
    public void destroy() {
        glfwDestroyWindow(handle);
        glfwTerminate();
    }
}
