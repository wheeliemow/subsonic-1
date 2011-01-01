/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */
package net.sourceforge.subsonic.androidapp.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import net.sourceforge.subsonic.androidapp.domain.MusicDirectory;
import net.sourceforge.subsonic.androidapp.domain.PlayerState;
import static net.sourceforge.subsonic.androidapp.domain.PlayerState.*;
import static net.sourceforge.subsonic.androidapp.domain.PlayerState.STARTED;
import net.sourceforge.subsonic.androidapp.util.CacheCleaner;
import net.sourceforge.subsonic.androidapp.util.FileUtil;
import net.sourceforge.subsonic.androidapp.util.Util;

/**
 * @author Sindre Mehus
 */
public class DownloadServiceLifecycleSupport {

    private static final String TAG = DownloadServiceLifecycleSupport.class.getSimpleName();
    private static final String FILENAME_DOWNLOADS_SER = "downloadstate.ser";

    private final DownloadServiceImpl downloadService;
    private ScheduledExecutorService executorService;
    private BroadcastReceiver headsetEventReceiver;
    private PhoneStateListener phoneStateListener;

    public DownloadServiceLifecycleSupport(DownloadServiceImpl downloadService) {
        this.downloadService = downloadService;
    }

    public void onCreate() {
        Runnable downloadChecker = new Runnable() {
            @Override
            public void run() {
                try {
                    downloadService.checkDownloads();
                } catch (Throwable x) {
                    Log.e(TAG, "checkDownloads() failed.", x);
                }
            }
        };

        Runnable cacheCleaner = new Runnable() {
            @Override
            public void run() {
                new CacheCleaner(downloadService, downloadService).clean();
            }
        };

        executorService = Executors.newScheduledThreadPool(2);
        executorService.scheduleWithFixedDelay(downloadChecker, 5, 5, TimeUnit.SECONDS);
        executorService.scheduleWithFixedDelay(cacheCleaner, 5 * 60, 60 * 60, TimeUnit.SECONDS);

        // Pause when headset is unplugged.
        headsetEventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "Headset event for: " + intent.getExtras().get("name"));
                if (intent.getExtras().getInt("state") == 0 && downloadService.getPlayerState() == PlayerState.STARTED) {
                    downloadService.pause();
                }
            }
        };
        downloadService.registerReceiver(headsetEventReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));

        // React to media buttons.
        Util.registerMediaButtonEventReceiver(downloadService);

        // Pause temporarily on incoming phone calls.
        phoneStateListener = new MyPhoneStateListener();
        TelephonyManager telephonyManager = (TelephonyManager) downloadService.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        deserializeDownloadQueue();
    }

    public void onStart(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            KeyEvent event = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (event != null) {
                handleKeyEvent(event);
            }
        }
    }

    public void onDestroy() {
        executorService.shutdown();
        serializeDownloadQueue();
        downloadService.clear(false);
        downloadService.unregisterReceiver(headsetEventReceiver);
//        Util.unregisterMediaButtonEventReceiver(downloadService);

        TelephonyManager telephonyManager = (TelephonyManager) downloadService.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    public void serializeDownloadQueue() {
        State state = new State();
        for (DownloadFile downloadFile : downloadService.getDownloads()) {
            state.songs.add(downloadFile.getSong());
        }
        state.currentPlayingIndex = downloadService.getCurrentPlayingIndex();
        state.currentPlayingPosition = downloadService.getPlayerPosition();

        FileUtil.serialize(downloadService, state, FILENAME_DOWNLOADS_SER);
    }

    private void deserializeDownloadQueue() {
       State state = FileUtil.deserialize(downloadService, FILENAME_DOWNLOADS_SER);
        if (state == null) {
            return;
        }
        downloadService.download(state.songs, false, false);
        if (state.currentPlayingIndex != -1) {
            downloadService.play(state.currentPlayingIndex, false);
        }
    }

    private void handleKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN || event.getRepeatCount() > 0) {
            return;
        }
        PlayerState state = downloadService.getPlayerState();
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                if (state == PAUSED || state == COMPLETED) {
                    downloadService.start();
                } else if (state == STOPPED || state == IDLE) {
                    int current = downloadService.getCurrentPlayingIndex();
                    if (current == -1) {
                        downloadService.play(0);
                    } else {
                        downloadService.play(current);
                    }
                } else if (state == STARTED) {
                    downloadService.pause();
                }

                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                downloadService.previous();
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                if (downloadService.getCurrentPlayingIndex() < downloadService.size() - 1) {
                    downloadService.next();
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_STOP:
                downloadService.reset();
                break;
            default:
        }
    }

    /**
     * Logic taken from packages/apps/Music.  Will pause when an incoming
     * call rings (volume > 0), or if a call (incoming or outgoing) is connected.
     */
    private class MyPhoneStateListener extends PhoneStateListener {
        private boolean resumeAfterCall;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    AudioManager am = (AudioManager) downloadService.getSystemService(Context.AUDIO_SERVICE);

                    // Don't pause if the ringer isn't making any noise.
                    int ringvol = am.getStreamVolume(AudioManager.STREAM_RING);
                    if (ringvol <= 0) {
                        break;
                    }

                    // Fall through...
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (downloadService.getPlayerState() == PlayerState.STARTED) {
                        resumeAfterCall = true;
                        downloadService.pause();
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (resumeAfterCall) {
                        resumeAfterCall = false;
                        downloadService.start();
                    }
                    break;
                default:
                    break;
            }
        }
    }


    private static class State implements Serializable {
        private List<MusicDirectory.Entry> songs = new ArrayList<MusicDirectory.Entry>();
        private int currentPlayingIndex;
        private int currentPlayingPosition;
    }
}