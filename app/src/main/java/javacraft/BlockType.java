package javacraft;

import org.joml.Vector3f;

/**
 * ブロックの「種類」を表す。共有される性質(当たり判定の有無、色)だけを持ち、
 * 個々のブロックごとの状態は持たない(YAGNI: 必要になるまでBlockクラスは作らない)。
 */
public enum BlockType {

    AIR(false, new Vector3f(0f, 0f, 0f)),
    GRASS(true, new Vector3f(0.2f, 0.8f, 0.2f)),
    DIRT(true, new Vector3f(0.5f, 0.3f, 0.1f)),
    STONE(true, new Vector3f(0.5f, 0.5f, 0.5f));

    private final boolean solid;
    private final Vector3f color;

    BlockType(boolean solid, Vector3f color) {
        this.solid = solid;
        this.color = color;
    }

    /** プレイヤーが通り抜けられない(当たり判定を持つ)かどうか。 */
    public boolean isSolid() {
        return solid;
    }

    public Vector3f getColor() {
        return color;
    }
}
