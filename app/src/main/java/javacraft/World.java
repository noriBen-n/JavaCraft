package javacraft;

/**
 * 固定サイズ(16x16x16)のブロック配置を保持する。
 * 将来のChunkシステムでは、この構造がそのまま1つのChunkの中身になる。
 */
public class World {

    public static final int SIZE = 16;

    private final BlockType[][][] blocks = new BlockType[SIZE][SIZE][SIZE];

    public World() {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    blocks[x][y][z] = BlockType.AIR;
                }
            }
        }
    }

    public BlockType getBlock(int x, int y, int z) {
        return blocks[x][y][z];
    }

    public void setBlock(int x, int y, int z, BlockType type) {
        blocks[x][y][z] = type;
    }

    /** 範囲外の座標はすべて「衝突しない(AIR扱い)」として安全に返す、当たり判定専用の問い合わせ。 */
    public boolean isSolid(int x, int y, int z) {
        if (!inBounds(x, y, z)) {
            return false;
        }
        return getBlock(x, y, z).isSolid();
    }

    /** この座標がWorldの範囲内かどうか。範囲外に破壊/設置しようとする操作を弾くのに使う。 */
    public boolean inBounds(int x, int y, int z) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE && z >= 0 && z < SIZE;
    }
}
