package javacraft;

import org.joml.Vector3f;

/**
 * 重力・地面との衝突など、Chunkの形状に基づく物理計算を担当する。
 * Player自身はBlockの配列を直接見ず、この計算結果(VerticalMotion)だけを受け取る。
 */
public class Physics {

    private static final float GRAVITY = -20.0f; // 1秒あたりの落下加速度(ワールド単位/秒^2)
    public static final float EYE_HEIGHT = 1.6f;  // 目線(Camera位置)は足元よりこれだけ高い
    private static final float PLAYER_HEIGHT = 1.8f; // 足元から頭までの高さ(水平衝突の判定範囲に使う)

    /** 1フレーム分の垂直移動を計算した結果。「何が起きたか」だけをPlayerに伝える。 */
    public record VerticalMotion(float velocityY, boolean grounded) {}

    /**
     * 重力を適用し、真下に固体Blockがあれば着地させる。position.y はこのメソッドが直接書き換える。
     */
    public VerticalMotion applyGravity(Chunk chunk, Vector3f position, float velocityY, float deltaTime) {
        velocityY += GRAVITY * deltaTime;
        float newY = position.y + velocityY * deltaTime;

        int feetX = (int) Math.floor(position.x);
        int feetZ = (int) Math.floor(position.z);
        int feetY = (int) Math.floor(newY - EYE_HEIGHT);

        if (chunk.isSolid(feetX, feetY, feetZ)) {
            // Blockの上面にちょうど乗る高さへ補正し、落下速度をリセットする
            position.y = feetY + 1 + EYE_HEIGHT;
            return new VerticalMotion(0f, true);
        }

        position.y = newY;
        return new VerticalMotion(velocityY, false);
    }

    /**
     * 水平方向(X, Z)の移動を、Blockとの衝突を見ながら適用する。position.x/zを直接書き換える。
     * X方向とZ方向を別々に判定することで、壁に斜めに当たった時に壁沿いに滑れるようにしている。
     */
    public void moveHorizontal(Chunk chunk, Vector3f position, float dx, float dz) {
        int lowY = (int) Math.floor(position.y - EYE_HEIGHT + 0.05f);
        int highY = (int) Math.floor(position.y - EYE_HEIGHT + PLAYER_HEIGHT - 0.05f);

        float newX = position.x + dx;
        if (!collidesHorizontally(chunk, newX, position.z, lowY, highY)) {
            position.x = newX;
        }

        float newZ = position.z + dz;
        if (!collidesHorizontally(chunk, position.x, newZ, lowY, highY)) {
            position.z = newZ;
        }
    }

    /** (x, z)の柱を、足元から頭までの高さの範囲でSolidかどうか調べる。 */
    private boolean collidesHorizontally(Chunk chunk, float x, float z, int lowY, int highY) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);

        for (int y = lowY; y <= highY; y++) {
            if (chunk.isSolid(blockX, y, blockZ)) {
                return true;
            }
        }
        return false;
    }
}
