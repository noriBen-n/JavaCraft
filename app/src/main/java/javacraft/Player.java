package javacraft;

import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;

/**
 * プレイヤーの移動ロジック(WASD)を担当する。見え方の計算は持たず、Cameraに委譲する。
 * 一人称視点なので「PlayerがCameraを1つ持つ」構成にしている。
 */
public class Player {

    private static final float MOVE_SPEED = 4.0f; // 1秒あたりの移動距離(ワールド単位)
    private static final float MOUSE_SENSITIVITY = 0.1f; // マウス1ピクセル移動あたりの角度(度)
    private static final float MAX_PITCH = 89f; // 真上・真下ぎりぎりまでで止め、視点反転を防ぐ
    private static final float JUMP_VELOCITY = 8.0f; // ジャンプ開始時の上向き速度
    private static final float REACH = 5.0f; // ブロック破壊/設置が届く最大距離
    private static final BlockType PLACE_BLOCK_TYPE = BlockType.STONE; // 今はインベントリが無いので固定

    private final Camera camera;
    private final Physics physics = new Physics();
    private final Raycaster raycaster = new Raycaster();

    // 前フレームのマウス座標。差分(移動量)を求めるために保持する。
    private double lastMouseX;
    private double lastMouseY;
    private boolean firstMouseInput = true; // 初回だけ、いきなり大きな差分が出るのを防ぐ

    private float verticalVelocity = 0f;
    private boolean grounded = false;

    // 「今押された瞬間」だけを検知するために、前フレームの押下状態を覚えておく
    private boolean leftMouseWasPressed = false;
    private boolean rightMouseWasPressed = false;

    public Player(Vector3f startPosition) {
        this.camera = new Camera(startPosition);
    }

    public Camera getCamera() {
        return camera;
    }

    /** 毎フレーム呼ぶ。deltaTimeにより、フレームレートが変わっても移動速度を一定に保つ。 */
    public void update(Window window, Chunk chunk, float deltaTime) {
        updateLook(window);

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

        if (grounded && window.isKeyPressed(GLFW_KEY_SPACE)) {
            verticalVelocity = JUMP_VELOCITY;
            grounded = false;
        }

        // 「何がどう衝突したか」の計算はPhysicsに任せ、その結果だけを自分の状態に反映する
        Physics.VerticalMotion motion = physics.applyGravity(chunk, position, verticalVelocity, deltaTime);
        verticalVelocity = motion.velocityY();
        grounded = motion.grounded();

        handleBlockInteraction(window, chunk);
    }

    /** 左クリックでBlockを破壊、右クリックで設置する。押しっぱなしで連打しないよう、押した瞬間だけ反応する。 */
    private void handleBlockInteraction(Window window, Chunk chunk) {
        boolean leftMousePressed = window.isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT);
        boolean rightMousePressed = window.isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT);

        boolean leftJustPressed = leftMousePressed && !leftMouseWasPressed;
        boolean rightJustPressed = rightMousePressed && !rightMouseWasPressed;
        leftMouseWasPressed = leftMousePressed;
        rightMouseWasPressed = rightMousePressed;

        if (!leftJustPressed && !rightJustPressed) {
            return; // 何もクリックされていなければRaycastすら不要
        }

        Optional<Raycaster.RaycastHit> hit = raycaster.cast(
                chunk, camera.getPosition(), camera.getLookDirection(), REACH);

        if (hit.isEmpty()) {
            return;
        }

        if (leftJustPressed) {
            Vector3i b = hit.get().hitBlock();
            chunk.setBlock(b.x, b.y, b.z, BlockType.AIR); // 破壊 = AIRに置き換える
        } else {
            Vector3i p = hit.get().placePosition();
            if (chunk.inBounds(p.x, p.y, p.z)) {
                chunk.setBlock(p.x, p.y, p.z, PLACE_BLOCK_TYPE);
            }
        }
    }

    /** マウスの移動量から、カメラのyaw(左右)とpitch(上下)を更新する。 */
    private void updateLook(Window window) {
        double[] cursor = window.getCursorPosition();
        double mouseX = cursor[0];
        double mouseY = cursor[1];

        if (firstMouseInput) {
            // 初回はまだ「前回の位置」が無いので、差分を計算せずに基準点として記録するだけにする
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            firstMouseInput = false;
            return;
        }

        double deltaX = mouseX - lastMouseX;
        double deltaY = mouseY - lastMouseY;
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        camera.setYaw(camera.getYaw() + (float) deltaX * MOUSE_SENSITIVITY);

        // 画面座標はYが下向きに増えるため、マウスを上に動かしたら見上げるように符号を反転する
        float newPitch = camera.getPitch() - (float) deltaY * MOUSE_SENSITIVITY;
        newPitch = Math.max(-MAX_PITCH, Math.min(MAX_PITCH, newPitch));
        camera.setPitch(newPitch);
    }
}
