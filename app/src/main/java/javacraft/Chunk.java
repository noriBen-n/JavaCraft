package javacraft;

/**
 * 固定サイズ(16x16x16)のブロック配置を保持する。
 * 今はWorld全体がこのChunk1つだけだが、将来は複数のChunkをMapで管理して
 * 無限に広いWorldへ拡張できる(そのときもこのクラス自体はほぼ変える必要がない)。
 */
public class Chunk {

    public static final int SIZE = 16;

    private final BlockType[][][] blocks = new BlockType[SIZE][SIZE][SIZE];

    // Block配置が変わったかどうか。trueの間はChunkMeshの再構築が必要、という合図に使う。
    // 初期状態もtrue: まだ一度もメッシュを作っていないので「作る必要がある」ため。
    private boolean dirty = true;

    public Chunk() {
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
        dirty = true;
    }

    /** ChunkMeshの再構築が必要かどうか。 */
    public boolean isDirty() {
        return dirty;
    }

    /** メッシュを再構築し終えたら呼ぶ。次にsetBlockが呼ばれるまでは再構築不要になる。 */
    public void clearDirty() {
        dirty = false;
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
