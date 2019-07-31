package com.busecarik.asteroidsgl;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

public class Jukebox {
    public enum GameEvent {
        Start,
        Bullet,
        Explosion,
        GameOver
    }

    private static final String TAG = "Jukebox";

    private SoundPool _soundPool = null;
    private boolean _soundEnabled = true;
    private HashMap<GameEvent, Integer> _soundsMap = null;
    private Context _context = null;
    private boolean _musicEnabled = true;
    private MediaPlayer _bgPlayer = null;

    public Jukebox(Context context) {
        _context = context;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        _soundEnabled = prefs.getBoolean(Config.SOUNDS_PREF_KEY, true);
        _musicEnabled = prefs.getBoolean(Config.MUSIC_PREF_KEY, true);
        loadIfNeeded();
    }

    private void loadMusic(){
        try{
            _bgPlayer = new MediaPlayer();
            AssetFileDescriptor afd = _context
                    .getAssets().openFd(Config.BACKGROUND_MUSIC);
            _bgPlayer.setDataSource(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength());
            _bgPlayer.setLooping(true);
            _bgPlayer.setVolume(Config.DEFAULT_MUSIC_VOLUME, Config.DEFAULT_MUSIC_VOLUME);
            _bgPlayer.prepare();
        }catch(IOException e){
            _bgPlayer = null;
            _musicEnabled = false;
            Log.e(TAG, "loadMusic: error loading music " + e.toString());
        }
    }

    public void pauseBgMusic(){
        if(!_musicEnabled){ return; }
        _bgPlayer.pause();
    }
    public void resumeBgMusic(){
        if(!_musicEnabled){ return; }
        _bgPlayer.start();
    }
    public void playSoundForGameEvent(GameEvent event){
        if(!_soundEnabled){return;}
        final float leftVolume = Config.DEFAULT_VOLUME;
        final float rightVolume = Config.DEFAULT_VOLUME;
        final int priority = 1;
        final int loop = 0;
        final float rate = 1.0f;
        final Integer soundID = _soundsMap.get(event);
        if(soundID != null){
            _soundPool.play(soundID, leftVolume, rightVolume, priority, loop, rate);
        }
    }

    private void loadIfNeeded(){
        if(_soundEnabled){
            loadSounds();
        }
        if(_musicEnabled){
            loadMusic();
        }
    }

    private void loadSounds(){
        createSoundPool();
        _soundsMap = new HashMap<GameEvent, Integer>();
        loadEventSound(GameEvent.Start, Config.START_LEVEL);
        loadEventSound(GameEvent.Bullet, Config.BULLET);
        loadEventSound(GameEvent.GameOver, Config.GAME_OVER);
        loadEventSound(GameEvent.Explosion, Config.EXPLOSION);
    }

    private void loadEventSound(final GameEvent event, final String fileName){
        try {
            AssetFileDescriptor afd = _context.getAssets().openFd(fileName);
            int soundId = _soundPool.load(afd, 1);
            _soundsMap.put(event, soundId);
        }catch(IOException e){
            Log.e(TAG, "loadEventSound: error loading sound " + e.toString());
        }
    }

    @SuppressWarnings("deprecation")
    private void createSoundPool() {
        AudioAttributes attr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        _soundPool = new SoundPool.Builder()
                .setAudioAttributes(attr)
                .setMaxStreams(Config.MAX_STREAMS)
                .build();
    }
}
