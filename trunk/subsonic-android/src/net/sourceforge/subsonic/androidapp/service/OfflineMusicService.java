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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import net.sourceforge.subsonic.androidapp.domain.Artist;
import net.sourceforge.subsonic.androidapp.domain.Indexes;
import net.sourceforge.subsonic.androidapp.domain.MusicDirectory;
import net.sourceforge.subsonic.androidapp.domain.MusicFolder;
import net.sourceforge.subsonic.androidapp.domain.Playlist;
import net.sourceforge.subsonic.androidapp.domain.SearchResult;
import net.sourceforge.subsonic.androidapp.domain.SearchCritera;
import net.sourceforge.subsonic.androidapp.util.Constants;
import net.sourceforge.subsonic.androidapp.util.FileUtil;
import net.sourceforge.subsonic.androidapp.util.ProgressListener;
import net.sourceforge.subsonic.androidapp.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * @author Sindre Mehus
 */
public class OfflineMusicService extends RESTMusicService {

    @Override
    public boolean isLicenseValid(Context context, ProgressListener progressListener) throws Exception {
        return true;
    }

    @Override
    public Indexes getIndexes(String musicFolderId, boolean refresh, Context context, ProgressListener progressListener) throws Exception {
        List<Artist> artists = new ArrayList<Artist>();
        File root = FileUtil.getMusicDirectory();
        for (File file : FileUtil.listFiles(root)) {
            if (file.isDirectory()) {
                Artist artist = new Artist();
                artist.setId(file.getPath());
                artist.setIndex(file.getName().substring(0, 1));
                artist.setName(file.getName());
                artists.add(artist);
            }
        }
        return new Indexes(0L, Collections.<Artist>emptyList(), artists);
    }

    @Override
    public MusicDirectory getMusicDirectory(String id, Context context, ProgressListener progressListener) throws Exception {
        File dir = new File(id);
        MusicDirectory result = new MusicDirectory();
        result.setName(dir.getName());

        Set<String> names = new HashSet<String>();

        for (File file : FileUtil.listMusicFiles(dir)) {
            String name = getName(file);
            if (name != null & !names.contains(name)) {
                names.add(name);
                result.addChild(createEntry(file, name));
            }
        }
        return result;
    }

    private String getName(File file) {
        String name = file.getName();
        if (file.isDirectory()) {
            return name;
        }

        if (name.endsWith(".partial") || name.equals(Constants.ALBUM_ART_FILE)) {
            return null;
        }

        name = name.replace(".complete", "");
        return FileUtil.getBaseName(name);
    }

    private MusicDirectory.Entry createEntry(File file, String name) {
        MusicDirectory.Entry entry = new MusicDirectory.Entry();
        entry.setDirectory(file.isDirectory());
        entry.setId(file.getPath());
        entry.setParent(file.getParent());
        entry.setSize(file.length());
        String root = FileUtil.getMusicDirectory().getPath();
        entry.setPath(file.getPath().replaceFirst("^" + root + "/" , ""));
        if (file.isFile()) {
            entry.setArtist(file.getParentFile().getParentFile().getName());
            entry.setAlbum(file.getParentFile().getName());
        }
        entry.setTitle(name);
        entry.setSuffix(FileUtil.getExtension(file.getName().replace(".complete", "")));

        File albumArt = FileUtil.getAlbumArtFile(entry);
        if (albumArt.exists()) {
            entry.setCoverArt(albumArt.getPath());
        }
        return entry;
    }

    @Override
    public Bitmap getCoverArt(Context context, MusicDirectory.Entry entry, int size, boolean saveToFile, ProgressListener progressListener) throws Exception {
        InputStream in = new FileInputStream(entry.getCoverArt());
        try {
            byte[] bytes = Util.toByteArray(in);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return Bitmap.createScaledBitmap(bitmap, size, size, true);
        } finally {
            Util.close(in);
        }
    }

    @Override
    public List<MusicFolder> getMusicFolders(Context context, ProgressListener progressListener) throws Exception {
        throw new RuntimeException("Music folders not available in offline mode");
    }

    @Override
    public SearchResult search(SearchCritera criteria, Context context, ProgressListener progressListener) throws Exception {
        throw new RuntimeException("Search not available in offline mode");
    }

    @Override
    public List<Playlist> getPlaylists(Context context, ProgressListener progressListener) throws Exception {
        throw new RuntimeException("Playlists not available in offline mode");
    }

    @Override
    public MusicDirectory getPlaylist(String id, Context context, ProgressListener progressListener) throws Exception {
        throw new RuntimeException("Playlists not available in offline mode");
    }

    @Override
    public void createPlaylist(String id, String name, List<MusicDirectory.Entry> entries, Context context, ProgressListener progressListener) throws Exception {
        throw new RuntimeException("Playlists not available in offline mode");
    }

    @Override
    public MusicDirectory getAlbumList(String type, int size, int offset, Context context, ProgressListener progressListener) throws Exception {
        throw new RuntimeException("Album lists not available in offline mode");
    }

    @Override
    public String getVideoUrl(Context context, String id) {
        throw new RuntimeException("Video playback not available in offline mode");
    }

    @Override
    public MusicDirectory getRandomSongs(int size, Context context, ProgressListener progressListener) throws Exception {
        File root = FileUtil.getMusicDirectory();
        List<File> children = new LinkedList<File>();
        listFilesRecursively(root, children);
        MusicDirectory result = new MusicDirectory();

        if (children.isEmpty()) {
            return result;
        }
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            File file = children.get(random.nextInt(children.size()));
            result.addChild(createEntry(file, getName(file)));
        }

        return result;
    }

    private void listFilesRecursively(File parent, List<File> children) {
        for (File file : FileUtil.listMusicFiles(parent)) {
            if (file.isFile()) {
                children.add(file);
            } else {
                listFilesRecursively(file, children);
            }
        }
    }
}
