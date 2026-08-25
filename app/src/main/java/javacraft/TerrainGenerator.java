package javacraft;

import org.joml.SimplexNoise;

/**
 * Simplex Noiseを使って、なめらかな高さの地形をChunkに書き込む。
 * 生成アルゴリズムをChunk自身から切り離す(ChunkはBlockの入れ物に徹する)。
 */
public class TerrainGenerator {

    // 値が小さいほど、地形がなだらかに(緩やかな丘に)なる。大きいほど激しく変化する。
    private static final float NOISE_SCALE = 0.1f;

    // Simplex Noiseの戻り値は-1.0〜1.0。それを実際の高さ(何マス積むか)の範囲に変換するための係数。
    private static final float HEIGHT_AMPLITUDE = 4.0f;
    private static final int BASE_HEIGHT = 4; // 一番低い場所でもこの高さまでは積む

    public void generate(Chunk chunk) {
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int height = calculateHeight(x, z);

                for (int y = 0; y < Chunk.SIZE; y++) {
                    chunk.setBlock(x, y, z, blockTypeAt(y, height));
                }
            }
        }
    }

    /** (x, z)の柱の高さを、Simplex Noiseから計算する。 */
    private int calculateHeight(int x, int z) {
        float noiseValue = SimplexNoise.noise(x * NOISE_SCALE, z * NOISE_SCALE); // -1.0 〜 1.0
        float height = BASE_HEIGHT + (noiseValue + 1f) / 2f * HEIGHT_AMPLITUDE; // 0 〜 (BASE+AMPLITUDE)
        return (int) height;
    }

    /** 地表からの深さに応じてBlockの種類を決める。表面はGRASS、その下はDIRT、さらに下はSTONE。 */
    private BlockType blockTypeAt(int y, int surfaceHeight) {
        if (y > surfaceHeight) {
            return BlockType.AIR;
        }
        if (y == surfaceHeight) {
            return BlockType.GRASS;
        }
        if (y >= surfaceHeight - 3) {
            return BlockType.DIRT;
        }
        return BlockType.STONE;
    }
}
