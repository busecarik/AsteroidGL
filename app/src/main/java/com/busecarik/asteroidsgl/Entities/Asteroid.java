package com.busecarik.asteroidsgl.Entities;

import android.opengl.GLES20;

import com.busecarik.asteroidsgl.Config;
import com.busecarik.asteroidsgl.Mesh;
import com.busecarik.asteroidsgl.Random;

public class Asteroid extends GLEntity {

    public enum Size {
        Small,
        Medium,
        Large
    }

    public Size _size;

    public Asteroid(final float x, final float y, Size size){
        _x = x;
        _y = y;
        _size = size;
        _width = Config.ASTEROID_SIZE;
        _height = Config.ASTEROID_SIZE;
        switch (size) {
            case Small:
                _scale = Config.SMALL_SCALE;
                _velX = Random.between(Config.VEL_MEDIUM, Config.VEL_SMALL) * Random.sign();
                _velY = Random.between(Config.VEL_MEDIUM, Config.VEL_SMALL) * Random.sign();
                _velR = Random.between(Config.VEL_MEDIUM*2, Config.VEL_SMALL*2) * Random.sign();
                break;
            case Medium:
                _scale = Config.MEDIUM_SCALE;
                _velX = Random.between(Config.VEL_LARGE, Config.VEL_MEDIUM) * Random.sign();
                _velY = Random.between(Config.VEL_LARGE, Config.VEL_MEDIUM) * Random.sign();
                _velR = Random.between(Config.VEL_LARGE*2, Config.VEL_MEDIUM*2) * Random.sign();
                break;
            case Large:
                _scale = Config.LARGE_SCALE;
                _velX = Random.between(Config.VEL_LARGE, Config.VEL_LARGE) * Random.sign();
                _velY = Random.between(Config.VEL_LARGE, Config.VEL_LARGE) * Random.sign();
                _velR = Random.between(Config.VEL_LARGE*2, Config.VEL_LARGE*2) * Random.sign();
                break;
        }
        final double radius = _width*0.5;
        final float[] verts = Mesh.generateLinePolygon(Config.POINT, radius);
        _mesh = new Mesh(verts, GLES20.GL_LINES);
        _mesh.setWidthHeight(_width, _height);
    }
}
