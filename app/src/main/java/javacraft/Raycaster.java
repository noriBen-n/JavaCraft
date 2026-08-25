package javacraft;

import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Optional;

/**
 * ある位置から、ある方向に伸ばした線が、Chunkの中で最初にどのBlockに当たるかを調べる。
 * 物理シミュレーション(Physics)とは別の関心事(「狙いをつける」)として切り出している。
 * 将来、Mobの視線判定や投擲物の着弾判定にも同じ考え方を使い回せる。
 */
public class Raycaster {

    private static final float STEP = 0.05f; // 光線を伸ばす1回あたりの刻み幅。細かいほど正確だが重くなる。

    /**
     * Raycastの結果。当たったBlockの座標(hitBlock)と、
     * その手前の空気マスの座標(placePosition、設置用)をセットで持つ。
     */
    public record RaycastHit(Vector3i hitBlock, Vector3i placePosition) {}

    /** originからdirection方向へ、maxDistanceまで探索する。何にも当たらなければ空を返す。 */
    public Optional<RaycastHit> cast(Chunk chunk, Vector3f origin, Vector3f direction, float maxDistance) {
        Vector3f pos = new Vector3f(origin);
        Vector3f step = new Vector3f(direction).normalize().mul(STEP);

        Vector3i previousBlock = null;

        for (float traveled = 0; traveled < maxDistance; traveled += STEP) {
            int x = (int) Math.floor(pos.x);
            int y = (int) Math.floor(pos.y);
            int z = (int) Math.floor(pos.z);

            if (chunk.isSolid(x, y, z)) {
                Vector3i hitBlock = new Vector3i(x, y, z);
                // 直前にいた(まだ空気だった)マスが、設置先の候補になる
                Vector3i placePosition = previousBlock != null ? previousBlock : hitBlock;
                return Optional.of(new RaycastHit(hitBlock, placePosition));
            }

            previousBlock = new Vector3i(x, y, z);
            pos.add(step);
        }

        return Optional.empty();
    }
}
