package com.busecarik.asteroidsgl;

public class Random {
    private final static java.util.Random RNG = new java.util.Random();
    public Random(){ super ();}

    public static float nextFloat(){
        return RNG.nextFloat();
    }

    public static int nextInt( final int max){
        return RNG.nextInt(max);
    }

    public static int between( final int min, final int max){
        return RNG.nextInt(max-min)+min;
    }

    public static float between( final float min, final float max){
        return min+RNG.nextFloat()*(max-min);
    }

    public static int sign() {
        if (RNG.nextInt() % 2 == 0) {
            return 1;
        }
        return -1;
    }
}
