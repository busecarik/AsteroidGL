package com.busecarik.asteroidsgl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;

import com.busecarik.asteroidsgl.Entities.Asteroid;
import com.busecarik.asteroidsgl.Entities.Border;
import com.busecarik.asteroidsgl.Entities.Bullet;
import com.busecarik.asteroidsgl.Entities.GLEntity;
import com.busecarik.asteroidsgl.Entities.Particle;
import com.busecarik.asteroidsgl.Entities.Player;
import com.busecarik.asteroidsgl.Entities.Star;
import com.busecarik.asteroidsgl.inputs.InputManager;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Game extends GLSurfaceView implements GLSurfaceView.Renderer{


    //trying a fixed time-step with accumulator, courtesy of
    //   https://gafferongames.com/post/fix_your_timestep/
    final double dt = 0.01;
    double accumulator = 0.0;
    double currentTime = System.nanoTime()*Config.NANOSECONDS_TO_SECONDS;
    private static final int BULLET_COUNT = (int)(Config.TIME_TO_LIVE/ Config.TIME_BETWEEN_SHOTS)+1;
    Bullet[] _bullets = new Bullet[BULLET_COUNT];
    Particle[] _particles = new Particle[Config.PARTICLE_COUNT];

    // Create the projection Matrix. This is used to project the scene onto a 2D viewport.
    private float[] _viewportMatrix = new float[4*4]; //In essence, it is our our Camera
    private Random r;
    private Border _border;
    private Player _player;
    private ArrayList<Star>_stars= new ArrayList<>();
    private ArrayList<Asteroid> _asteroids = new ArrayList<>();
    public InputManager _inputs = new InputManager();
    public Jukebox _jukebox = null;
    public Context _context = null;
    private HUD _hud = null;
    public int _score = 0;
    public int _level = 1;
    boolean _isGameOver = false;
    boolean _isBreak = false;

    public Game(Context context) {
        super(context);
        init(context);
    }

    public Game(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        _context = context;
        GLEntity._game = this;
        for(int i = 0; i < BULLET_COUNT; i++) {
            _bullets[i] = new Bullet();
        }
        for (int i = 0; i < Config.PARTICLE_COUNT; i++) {
            _particles[i] = new Particle();
        }
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true); //context *may* be preserved and thus *may* avoid slow reloads when switching apps.
        // we always re-create the OpenGL context in onSurfaceCreated, so we're safe either way.

        _jukebox = new Jukebox(context);
        _hud = new HUD(context);

        setRenderer(this);
    }

    public void setControls(final InputManager input){
        _inputs = input;
    }

    private void update(){
        final double newTime = System.nanoTime()*Config.NANOSECONDS_TO_SECONDS;
        final double frameTime = newTime - currentTime;
        currentTime = newTime;
        accumulator += frameTime;
        while(accumulator >= dt){
            for(final Asteroid a : _asteroids) {
                if(a.isDead()){continue;}
                a.update(dt);
                if(_player.isColliding(a)){
                    _player.onCollision(a);
                    a.onCollision(_player);
                }
            }
            for(final Bullet b : _bullets){
                if(b.isDead()){ continue; } //skip
                b.update(dt);
            }
            for (final Particle p : _particles) {
                if (p.isDead()) { continue; }
                p.update(dt);
            }
            _player.update(dt);

            collisionDetection();
            removeDeadEntities();
            checkAsteroids();
            checkGameOver();
            //_updateCount++;
            accumulator -= dt;
        }
        //setViewport(_player._x, _player._y);
    }

    private void collisionDetection(){
        for(final Bullet b : _bullets) {
            if(b.isDead()){ continue; } //skip dead bullets
            for(final Asteroid a : _asteroids) {
                if(b.isColliding(a)){
                    if(a.isDead()){continue;}
                    onGameEvent(Jukebox.GameEvent.Bullet, a);
                    b.onCollision(a); //notify each entity so they can decide what to do
                    b._ttl = 0;
                    switch (a._size) {
                        case Small:
                            _score = _score + Config.SMALL_SCORE;
                            break;
                        case Medium:
                            _score = _score + Config.MEDIUM_SCORE;
                            _asteroids.add(new Asteroid(a._x, a._y, Asteroid.Size.Small));
                            _asteroids.add(new Asteroid(a._x, a._y, Asteroid.Size.Small));
                            _isBreak = true;
                            break;
                        case Large:
                            _score = _score + Config.LARGE_SCORE;
                            _asteroids.add(new Asteroid(a._x, a._y, Asteroid.Size.Medium));
                            _asteroids.add(new Asteroid(a._x, a._y, Asteroid.Size.Medium));
                            _isBreak = true;
                            break;
                    }
                    particleSpread(a);
                    a.onCollision(b);
                }
                if (_isBreak) {
                    _isBreak = false;
                    break;
                }
            }
        }
    }

    public void checkGameOver() {
        if (_player._health < 1) {
            _isGameOver = true;
            _level = 1;
            _score = 0;
            _asteroids.clear();
            clearBullets();
            _player._health = Config.PLAYER_HEALTH;
            _player._x = Config.WORLD_WIDTH/2f;
            _player._y = Config.WORLD_HEIGHT/2f;
            createAllAsteroids();
            _isGameOver = false;
        }
    }

    public void clearBullets() {
        for (int i = 0; i < BULLET_COUNT; i++) {
            _bullets[i]._ttl = 0;
        }
    }

    public void checkAsteroids() {
        if (_asteroids.isEmpty() && _player._health > 0 && !_isBreak) {
            onGameEvent(Jukebox.GameEvent.Start, null);
            clearBullets();
            _level++;
            createAllAsteroids();
        }
    }

    public void createAllAsteroids() {
        for (int i = 0; i < _level; i++) {
            _asteroids.add(new Asteroid(r.nextInt((int) Config.WORLD_WIDTH), r.nextInt((int) Config.WORLD_HEIGHT), Asteroid.Size.Small));
        }
        for (int i = 0; i < _level; i++) {
            _asteroids.add(new Asteroid(r.nextInt((int) Config.WORLD_WIDTH), r.nextInt((int) Config.WORLD_HEIGHT), Asteroid.Size.Medium));
        }
        for (int i = 0; i < _level; i++) {
            _asteroids.add(new Asteroid(r.nextInt((int) Config.WORLD_WIDTH), r.nextInt((int) Config.WORLD_HEIGHT),  Asteroid.Size.Large));
        }
    }

    public void removeDeadEntities(){
        Asteroid temp;
        final int count = _asteroids.size();
        for(int i = count-1; i >= 0; i--){
            temp = _asteroids.get(i);
            if(temp.isDead()){
                _asteroids.remove(i);
            }
        }
    }

    private void render(){
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT); //clear buffer to background color
        //setup a projection matrix by passing in the range of the game world that will be mapped by OpenGL to the screen.
        //TODO: encapsulate this in a Camera-class, let it "position" itself relative to an entity
        final int offset = 0;
        final float left = 0;
        final float right = Config.METERS_TO_SHOW_X;
        final float bottom = Config.METERS_TO_SHOW_Y;
        final float top = 0;
        final float near = 0f;
        final float far = 1f;
        Matrix.orthoM(_viewportMatrix, offset, left, right, bottom, top, near, far);

        for(final Bullet b : _bullets){
            if(b.isDead()){ continue; } //skip
            b.render(_viewportMatrix);
        }
        for (final Particle p : _particles) {
            if(p.isDead()){ continue; }
            p.render(_viewportMatrix);
        }
        _border.render(_viewportMatrix);
        for(final Asteroid a : _asteroids){
            a.render(_viewportMatrix);
        }
        for(final Star s : _stars){
            s.render(_viewportMatrix);
        }
        _player.render(_viewportMatrix);
        _hud.update(_score,_level,_player._health, _player._hyperspace);
        _hud.render(_viewportMatrix);
    }

    public void particleSpread(final GLEntity source) {
        for (final Particle p : _particles) {
            if (p.isDead()) {
                p.spread(source);
            }
        }
    }

    public boolean maybeFireBullet(final GLEntity source){
        for(final Bullet b : _bullets) {
            if(b.isDead()) {
                b.fireFrom(source);
                return true;
            }
        }
        return false;
    }

    public void onGameEvent(Jukebox.GameEvent gameEvent, GLEntity e /*can be null!*/) {
        _jukebox.playSoundForGameEvent(gameEvent);
    }

    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        GLManager.buildProgram(_context); //compile, link and upload our GL program
        GLES20.glClearColor(Config.BG_COLOR[0], Config.BG_COLOR[1], Config.BG_COLOR[2], Config.BG_COLOR[3]); //set clear color
        _jukebox.resumeBgMusic();
        // center the player in the world.
        _player = new Player(Config.WORLD_WIDTH/2f, Config.WORLD_HEIGHT/2f);
        //Keep the Border inside the game view
        _border = new Border(Config.WORLD_WIDTH/2, Config.WORLD_HEIGHT/2, Config.WORLD_WIDTH, Config.WORLD_HEIGHT);
        r = new Random();
        for(int i = 0; i < Config.STAR_COUNT; i++){
            _stars.add(new Star(r.nextInt((int)Config.WORLD_WIDTH), r.nextInt((int)Config.WORLD_HEIGHT)));
        }
        createAllAsteroids();
    }

    @Override
    public void onSurfaceChanged(final GL10 unused, final int width, final int height) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(final GL10 unused) {
        update();
        render();
    }
}
