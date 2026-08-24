package javacraft;

import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

/**
 * プレイヤーの移動ロジック(WASD)を担当する。見え方の計算は持たず、Cameraに委譲する。
 * 一人称視点なので「PlayerがCameraを1つ持つ」構成にしている。
 */
public class Player {

    private static final float MOVE_SPEED = 4.0f; // 1秒あたりの移動距離(ワールド単位)

    private final Camera camera;

    public Player(Vector3f startPosition) {
        this.camera = new Camera(startPosition);
    }

    public Camera getCamera() {
        return camera;
    }

    /** 毎フレーム呼ぶ。deltaTimeにより、フレームレートが変わっても移動速度を一定に保つ。 */
    public void update(Window window, float deltaTime) {
        Vector3f forward = camera.getForward();
        Vector3f right = camera.getRight();
        Vector3f position = camera.getPosition(); // 内部のVector3fそのものを受け取り、直接動かす

        float distance = MOVE_SPEED * deltaTime;

        if (window.isKeyPressed(GLFW_KEY_W)) {
            position.add(forward.x * distance, 0f, forward.z * distance);
        }
        if (window.isKeyPressed(GLFW_KEY_S)) {
            position.add(-forward.x * distance, 0f, -forward.z * distance);
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            position.add(-right.x * distance, 0f, -right.z * distance);
        }
        if (window.isKeyPressed(GLFW_KEY_D)) {
            position.add(right.x * distance, 0f, right.z * distance);
        }
    }
}
