package com.busecarik.asteroidsgl.Entities;

import android.graphics.PointF;
import android.opengl.GLES20;

import com.busecarik.asteroidsgl.CollisionDetection;
import com.busecarik.asteroidsgl.Config;
import com.busecarik.asteroidsgl.Game;
import com.busecarik.asteroidsgl.Jukebox;
import com.busecarik.asteroidsgl.Mesh;
import com.busecarik.asteroidsgl.Random;
import com.busecarik.asteroidsgl.Utils;

public class Player extends GLEntity {
    private static final String TAG = "Player";
    private float _bulletCooldown = 0;
    public int _health = Config.PLAYER_HEALTH;
    private int _gameScore = 0;
    private long _timerForHyperspace = 0;
    private boolean _isPressC = false;
    public String _hyperspace = "";

    public Player(float x, float y){
        super();
        _x = x;
        _y = y;
        _width = Config.PLAYER_WIDTH;
        _height = Config.PLAYER_HEIGHT;
        float vertices[] = { // in counterclockwise order:
                0.0f,  0.5f, 0.0f, 	// top
                -0.5f, -0.5f, 0.0f,	// bottom left
                0.5f, -0.5f, 0.0f,  	// bottom right
        };
        _mesh = new Mesh(vertices, GLES20.GL_TRIANGLES);
        _mesh.setWidthHeight(_width, _height);
        _mesh.flipY();
    }

    @Override
    public void update(double dt) {
        _rotation += (dt*Config.ROTATION_VELOCITY) * _game._inputs._horizontalFactor;
        if(_game._inputs._pressingB){
            final float theta = _rotation*(float) Utils.TO_RAD;
            _velX += (float)Math.sin(theta) * Config.THRUST;
            _velY -= (float)Math.cos(theta) * Config.THRUST;
        }
        _velX *= Config.DRAG;
        _velY *= Config.DRAG;
        _bulletCooldown -= dt;
        if(_game._inputs._pressingA && _bulletCooldown <= 0){
            if(_game.maybeFireBullet(this)){
                _bulletCooldown = Config.TIME_BETWEEN_SHOTS;
            }
        }
        if (_game._inputs._pressingC) {
            _timerForHyperspace = System.currentTimeMillis();
            _x = Config.PLAYER_OUT_OF_BOUNDS;
            _y = Config.PLAYER_OUT_OF_BOUNDS;
            _hyperspace = Config.HYPERSPACE_ON;
            _isPressC = true;
        }
        if (System.currentTimeMillis() > _timerForHyperspace + Config.TIME_FOR_HYPERSPACE && _isPressC) {
            _x = Random.nextInt((int) Config.WORLD_WIDTH);
            _y = Random.nextInt((int)Config.WORLD_HEIGHT);
            _hyperspace = Config.HYPERSPACE_OFF;
            _isPressC = false;
        }
        if (_game._score - _gameScore > Config.SCORE_GOAL) {
            _health++;
            _gameScore = _game._score;
        }
        super.update(dt);
    }

    @Override
    public void render(float[] viewportMatrix) {
        _scale = 1f;
        if (!_isPressC) {
            super.render(viewportMatrix);
        }
    }

    @Override
    public boolean isColliding(final GLEntity that){
        if(!areBoundingSpheresOverlapping(this, that)){
            return false;
        }
        final PointF[] shipHull = getPointList();
        final PointF[] asteroidHull = that.getPointList();
        if(CollisionDetection.polygonVsPolygon(shipHull, asteroidHull) || CollisionDetection.polygonVsPoint(asteroidHull, _x, _y)){
            _health--;
            _x = Config.WORLD_WIDTH/2f;
            _y = Config.WORLD_HEIGHT/2f;
            _game.onGameEvent(Jukebox.GameEvent.Explosion, this);
            return true;
        }
        return false;
    }

    static boolean areBoundingSpheresOverlapping(final GLEntity a, final GLEntity b) {
        final float dx = a.centerX()-b.centerX(); //delta x
        final float dy = a.centerY()-b.centerY();
        final float distanceSq = (dx*dx + dy*dy);
        final float minDistance = a.radius() + b.radius();
        final float minDistanceSq = minDistance*minDistance;
        return distanceSq < minDistanceSq;
    }
}
