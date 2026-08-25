package javacraft;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 「見え方」だけを担当するクラス。位置(position)と向き(yaw/pitch)から
 * View行列を計算する。移動ロジック(WASD、重力など)は持たない。
 */
public class Camera {

    private final Vector3f position;

    // yaw: 水平方向の向き(度)。-90度で-Z方向を向く(JOMLの座標系での「正面」に合わせるため)。
    private float yaw = -90f;
    // pitch: 上下方向の向き(度)。0度で水平を見る。
    private float pitch = 0f;

    public Camera(Vector3f position) {
        this.position = position;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /** 水平面(XZ平面)上での前方向。WASD移動で「上下に飛ばない」ようにpitchを無視する。 */
    public Vector3f getForward() {
        float yawRad = (float) Math.toRadians(yaw);
        return new Vector3f((float) Math.cos(yawRad), 0f, (float) Math.sin(yawRad)).normalize();
    }

    /** 前方向に対して右方向。左右移動(A/D)に使う。 */
    public Vector3f getRight() {
        return getForward().cross(new Vector3f(0f, 1f, 0f)).normalize();
    }

    /** マウス視点(上下左右を見回す)を含めた、実際に見ている方向。Raycastの照準方向にも使う。 */
    public Vector3f getLookDirection() {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        return new Vector3f(
                (float) (Math.cos(yawRad) * Math.cos(pitchRad)),
                (float) Math.sin(pitchRad),
                (float) (Math.sin(yawRad) * Math.cos(pitchRad))
        ).normalize();
    }

    public Matrix4f getViewMatrix() {
        Vector3f target = new Vector3f(position).add(getLookDirection());
        return new Matrix4f().lookAt(position, target, new Vector3f(0f, 1f, 0f));
    }
}
