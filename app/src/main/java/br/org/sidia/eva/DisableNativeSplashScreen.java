package br.org.sidia.eva;

import com.samsungxr.SXRMain;

public abstract class DisableNativeSplashScreen extends SXRMain {

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

}
