package com.busecarik.asteroidsgl.Entities;

import android.opengl.GLES20;

import com.busecarik.asteroidsgl.Config;
import com.busecarik.asteroidsgl.Mesh;
import com.busecarik.asteroidsgl.Random;

public class Particle extends GLEntity {
    public float _ttl = Config.TIME_TO_LIVE_PARTICLE;
    private static Mesh BULLET_MESH = new Mesh(Mesh.POINT, GLES20.GL_POINTS);

    public Particle() {

        _velX = Random.between(Config.VEL_MEDIUM, Config.VEL_MEDIUM) * Random.sign();
        _velY = Random.between(Config.VEL_MEDIUM, Config.VEL_MEDIUM) * Random.sign();
        _velR = Random.between(Config.VEL_MEDIUM*2, Config.VEL_MEDIUM*2) * Random.sign();
        setColors(1, 0, 1, 1);
        _mesh = BULLET_MESH;
    }

    public void spread(GLEntity source) {
        _x = source._x;
        _y = source._y;
        _scale = Config.MEDIUM_SCALE;
        _ttl = Config.TIME_TO_LIVE_PARTICLE;
    }

    @Override
    public void update(double dt) {
        if(_ttl > 0) {
            _ttl -= dt;
            super.update(dt);
        }
    }
    @Override
    public void render(final float[] viewportMatrix){
        if(_ttl > 0) {
            super.render(viewportMatrix);
        }
    }

    @Override
    public boolean isDead(){
        return _ttl < 1;
    }
}
