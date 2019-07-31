package com.busecarik.asteroidsgl;

import android.content.Context;

import java.util.ArrayList;

public class HUD {
    private ArrayList<Text> _texts = new ArrayList<>();
    private int _fps = 0;
    private int _fpsAverage = 0;
    private long _timer = 0;
    Context _context;
    private long framesTimer = 0;

    public HUD (Context context) {
        _context = context;
        _texts.add(new Text(String.valueOf(0), Config.TEXT_SCORE,Config.TEXT_HEIGHT));
        _texts.add(new Text(String.valueOf(Config.PLAYER_HEALTH), Config.TEXT_HEALTH, Config.TEXT_HEIGHT));
        _texts.add(new Text(String.valueOf(1), Config.TEXT_LEVEL,Config.TEXT_HEIGHT));
        _texts.add(new Text(String.valueOf(_fpsAverage), Config.TEXT_FPS, Config.WORLD_HEIGHT-Config.TEXT_HEIGHT));

        _texts.add(new Text(context.getString(R.string.score), Config.SCORE_TEXT, Config.TEXT_HEIGHT));
        _texts.add(new Text(context.getString(R.string.level), Config.LEVEL_TEXT, Config.TEXT_HEIGHT));
        _texts.add(new Text(context.getString(R.string.health), Config.TEXT_HEIGHT, Config.TEXT_HEIGHT));
        _texts.add(new Text(context.getString(R.string.fps), Config.TEXT_HEIGHT, Config.WORLD_HEIGHT-Config.TEXT_HEIGHT));
        _texts.add(new Text(_context.getString(R.string.hyperspace), Config.HYPERSPACE_TEXT,Config.WORLD_HEIGHT-Config.TEXT_HEIGHT));
    }

    public void update(final int score, final int level, final int health, final String hyperspace) {
        setFps();
        _texts.get(0).setString(String.valueOf(score));
        _texts.get(1).setString(String.valueOf(health));
        _texts.get(2).setString(String.valueOf(level));
        _texts.get(3).setString(String.valueOf(_fpsAverage));
        _texts.get(8).setString(_context.getString(R.string.hyperspace) + hyperspace);
    }

    public void render(final float[] viewportMatrix) {
        for (final Text t : _texts) {
            t.render(viewportMatrix);
        }
    }

    private void setFps() {
        //borrow code
        _timer = System.currentTimeMillis();
        _fps++;
        if (_timer - framesTimer > Config.FPS_TIMER) {
            framesTimer = _timer;
            _fpsAverage = _fps;
            _fps = 0;
        }
    }
}
