package elegion.com.roomdatabase;

import android.arch.persistence.room.Room;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import elegion.com.roomdatabase.database.Album;
import elegion.com.roomdatabase.database.MusicDao;
import elegion.com.roomdatabase.database.MusicDatabase;

public class MusicProvider extends ContentProvider {

    private static final String TAG = MusicProvider.class.getSimpleName();

    private static final String AUTHORITY = "com.elegion.roomdatabase.musicprovider";

    private static final String TABLE_ALBUM = "album";
    private static final String TABLE_SONG = "song";
    private static final String TABLE_ALBUM_SONG = "albumsong";

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int ALBUM_TABLE_CODE = 101;
    public static final int ALBUM_ROW_CODE = 102;

    public static final int SONG_TABLE_CODE = 201;
    public static final int SONG_ROW_CODE = 202;

    public static final int ALBUM_SONG_TABLE_CODE = 301;
    public static final int ALBUM_SONG_ROW_CODE = 302;

    public static final int ALBUM_SONG_ALBUM_CODE = 303;
    public static final int ALBUM_SONG_SONG_CODE = 304;

    public static Integer[] validCodes =
           {ALBUM_TABLE_CODE,
            ALBUM_ROW_CODE,
            SONG_TABLE_CODE,
            SONG_ROW_CODE,
            ALBUM_SONG_TABLE_CODE,
            ALBUM_SONG_ROW_CODE,
            ALBUM_SONG_ALBUM_CODE,
            ALBUM_SONG_SONG_CODE};

    public static String[] tableNames =
            {TABLE_ALBUM,
            TABLE_ALBUM,
            TABLE_SONG,
            TABLE_SONG,
            TABLE_ALBUM_SONG,
            TABLE_ALBUM_SONG,
            TABLE_ALBUM_SONG,
            TABLE_ALBUM_SONG};

    public static List<Integer> validCodesList = new ArrayList<Integer>(Arrays.asList(validCodes));


    static {
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM, ALBUM_TABLE_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM + "/*", ALBUM_ROW_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_SONG, SONG_TABLE_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_SONG + "/*", SONG_ROW_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM_SONG, ALBUM_SONG_TABLE_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM_SONG + "/" + ALBUM_ROW_CODE ,ALBUM_SONG_ALBUM_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM_SONG + "/" + SONG_ROW_CODE, ALBUM_SONG_SONG_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM_SONG + "/*", ALBUM_SONG_ROW_CODE);
    }

    private MusicDao mMusicDao;

    public MusicProvider() {
    }

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            mMusicDao = Room.databaseBuilder(getContext().getApplicationContext(), MusicDatabase.class, "music_database")
                    .build()
                    .getMusicDao();
            return true;
        }

        return false;
    }

    @Override
    public String getType(Uri uri) {
        int value = URI_MATCHER.match(uri);
        if (!validCodesList.contains(value)) throw new UnsupportedOperationException("not yet implemented");

        return tableNames[validCodesList.indexOf(value)];
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        int code = URI_MATCHER.match(uri);
        if (!validCodesList.contains(code)) return null;

        Cursor cursor;
        switch (code) {
            case ALBUM_TABLE_CODE:
                cursor = mMusicDao.getAlbumsCursor();
                break;
            case ALBUM_ROW_CODE:
                cursor = mMusicDao.getAlbumWithIdCursor((int) ContentUris.parseId(uri));
                break;
            case SONG_TABLE_CODE:
                cursor = mMusicDao.getSongsCursor();
                break;
            case SONG_ROW_CODE:
                cursor = mMusicDao.getSongWithIdCursor((int) ContentUris.parseId(uri));
                break;
            case ALBUM_SONG_TABLE_CODE:
                cursor = mMusicDao.getAlbumSongsCursor();
                break;
            case ALBUM_SONG_ROW_CODE:
                cursor = mMusicDao.getAlbumSongWithIdCursor((int) ContentUris.parseId(uri));
                break;
            case ALBUM_SONG_ALBUM_CODE:
                cursor = mMusicDao.getAlbumSongWithAlbumIdCursor((int) ContentUris.parseId(uri));
                break;
            case ALBUM_SONG_SONG_CODE:
                cursor = mMusicDao.getAlbumSongWithSongIdCursor((int) ContentUris.parseId(uri));
                break;
                default:
                    cursor = null;
                    break;
        }

        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (URI_MATCHER.match(uri) == ALBUM_TABLE_CODE && isValuesValid(values)) {
            Album album = new Album();
            Integer id = values.getAsInteger("id");
            album.setId(id);
            album.setName(values.getAsString("name"));
            album.setReleaseDate(values.getAsString("release"));
            mMusicDao.insertAlbum(album);
            return ContentUris.withAppendedId(uri, id);
        } else {
            throw new IllegalArgumentException("cant add multiple items");
        }
    }

    private boolean isValuesValid(ContentValues values) {
        return values.containsKey("id") && values.containsKey("name") && values.containsKey("release");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (URI_MATCHER.match(uri) == ALBUM_ROW_CODE && isValuesValid(values)) {
            Album album = new Album();
            int id = (int) ContentUris.parseId(uri);
            album.setId(id);
            album.setName(values.getAsString("name"));
            album.setReleaseDate(values.getAsString("release"));
            int updatedRows = mMusicDao.updateAlbumInfo(album);
            return updatedRows;
        } else {
            throw new IllegalArgumentException("cant add multiple items");
        }

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (URI_MATCHER.match(uri) == ALBUM_ROW_CODE) {
            int id = (int) ContentUris.parseId(uri);
            return mMusicDao.deleteAlbumById(id);
        } else {
            throw new IllegalArgumentException("cant add multiple items");
        }

    }
}
