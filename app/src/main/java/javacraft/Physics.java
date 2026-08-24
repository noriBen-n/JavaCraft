package javacraft;

import org.joml.Vector3f;

/**
 * 重力・地面との衝突など、Worldの形状に基づく物理計算を担当する。
 * Player自身はBlockの配列を直接見ず、この計算結果(VerticalMotion)だけを受け取る。
 */
public class Physics {

    private static final float GRAVITY = -20.0f; // 1秒あたりの落下加速度(ワールド単位/秒^2)
    public static final float EYE_HEIGHT = 1.6f;  // 目線(Camera位置)は足元よりこれだけ高い

    /** 1フレーム分の垂直移動を計算した結果。「何が起きたか」だけをPlayerに伝える。 */
    public record VerticalMotion(float velocityY, boolean grounded) {}

    /**
     * 重力を適用し、真下に固体Blockがあれば着地させる。position.y はこのメソッドが直接書き換える。
     */
    public VerticalMotion applyGravity(World world, Vector3f position, float velocityY, float deltaTime) {
        velocityY += GRAVITY * deltaTime;
        float newY = position.y + velocityY * deltaTime;

        int feetX = (int) Math.floor(position.x);
        int feetZ = (int) Math.floor(position.z);
        int feetY = (int) Math.floor(newY - EYE_HEIGHT);

        if (world.isSolid(feetX, feetY, feetZ)) {
            // Blockの上面にちょうど乗る高さへ補正し、落下速度をリセットする
            position.y = feetY + 1 + EYE_HEIGHT;
            return new VerticalMotion(0f, true);
        }

        position.y = newY;
        return new VerticalMotion(velocityY, false);
    }
}
