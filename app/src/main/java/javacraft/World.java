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
}
