package com.ruby.driveencrypt.lockscreen.fingerprint;

/**
 * Created by aleksandr on 2018/02/14.
 */

public interface PFFingerprintAuthListener {

    void onAuthenticated();

    void onError();
}