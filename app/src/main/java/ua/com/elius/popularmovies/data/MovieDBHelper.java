package ua.com.elius.popularmovies.data;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.DefaultDatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import ua.com.elius.popularmovies.BuildConfig;
import ua.com.elius.popularmovies.data.listpopular.ListPopularColumns;
import ua.com.elius.popularmovies.data.listtoprated.ListTopRatedColumns;
import ua.com.elius.popularmovies.data.movie.MovieColumns;
import ua.com.elius.popularmovies.data.review.ReviewColumns;
import ua.com.elius.popularmovies.data.video.VideoColumns;

public class MovieDBHelper extends SQLiteOpenHelper {
    private static final String TAG = MovieDBHelper.class.getSimpleName();

    public static final String DATABASE_FILE_NAME = "main.db";
    private static final int DATABASE_VERSION = 1;
    private static MovieDBHelper sInstance;
    private final Context mContext;
    private final MovieDBHelperCallbacks mOpenHelperCallbacks;

    // @formatter:off
    public static final String SQL_CREATE_TABLE_LIST_POPULAR = "CREATE TABLE IF NOT EXISTS "
            + ListPopularColumns.TABLE_NAME + " ( "
            + ListPopularColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ListPopularColumns.TMDB_MOVIE_ID + " INTEGER NOT NULL "
            + ", CONSTRAINT unique_tmdb_movie_id UNIQUE (tmdb_movie_id) ON CONFLICT REPLACE"
            + " );";

    public static final String SQL_CREATE_TABLE_LIST_TOP_RATED = "CREATE TABLE IF NOT EXISTS "
            + ListTopRatedColumns.TABLE_NAME + " ( "
            + ListTopRatedColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ListTopRatedColumns.TMDB_MOVIE_ID + " INTEGER NOT NULL "
            + ", CONSTRAINT unique_tmdb_movie_id UNIQUE (tmdb_movie_id) ON CONFLICT REPLACE"
            + " );";

    public static final String SQL_CREATE_TABLE_MOVIE = "CREATE TABLE IF NOT EXISTS "
            + MovieColumns.TABLE_NAME + " ( "
            + MovieColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MovieColumns.TMDB_MOVIE_ID + " INTEGER NOT NULL, "
            + MovieColumns.POPULARITY + " REAL NOT NULL, "
            + MovieColumns.VOTE_AVERAGE + " REAL NOT NULL, "
            + MovieColumns.VOTE_COUNT + " INTEGER NOT NULL, "
            + MovieColumns.TITLE + " TEXT NOT NULL, "
            + MovieColumns.OVERVIEW + " TEXT NOT NULL, "
            + MovieColumns.POSTER_PATH + " TEXT NOT NULL, "
            + MovieColumns.BACKDROP_PATH + " TEXT NOT NULL, "
            + MovieColumns.RELEASE_DATE + " TEXT NOT NULL, "
            + MovieColumns.LIKE + " INTEGER NOT NULL DEFAULT 0 "
            + ", CONSTRAINT unique_tmdb_movie_id UNIQUE (tmdb_movie_id) ON CONFLICT REPLACE"
            + " );";

    public static final String SQL_CREATE_TABLE_REVIEW = "CREATE TABLE IF NOT EXISTS "
            + ReviewColumns.TABLE_NAME + " ( "
            + ReviewColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ReviewColumns.MOVIE_ID + " INTEGER NOT NULL, "
            + ReviewColumns.TMDB_REVIEW_ID + " TEXT NOT NULL, "
            + ReviewColumns.AUTHOR + " TEXT NOT NULL, "
            + ReviewColumns.CONTENT + " TEXT NOT NULL, "
            + ReviewColumns.URL + " TEXT NOT NULL "
            + ", CONSTRAINT fk_movie_id FOREIGN KEY (" + ReviewColumns.MOVIE_ID + ") REFERENCES movie (_id) ON DELETE CASCADE"
            + ", CONSTRAINT unique_tmdb_review_id UNIQUE (tmdb_review_id) ON CONFLICT REPLACE"
            + " );";

    public static final String SQL_CREATE_TABLE_VIDEO = "CREATE TABLE IF NOT EXISTS "
            + VideoColumns.TABLE_NAME + " ( "
            + VideoColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + VideoColumns.MOVIE_ID + " INTEGER NOT NULL, "
            + VideoColumns.TMDB_VIDEO_ID + " TEXT NOT NULL, "
            + VideoColumns.KEY + " TEXT NOT NULL, "
            + VideoColumns.NAME + " TEXT NOT NULL, "
            + VideoColumns.SITE + " TEXT NOT NULL, "
            + VideoColumns.SIZE + " INTEGER NOT NULL, "
            + VideoColumns.TYPE + " TEXT NOT NULL "
            + ", CONSTRAINT fk_movie_id FOREIGN KEY (" + VideoColumns.MOVIE_ID + ") REFERENCES movie (_id) ON DELETE CASCADE"
            + ", CONSTRAINT unique_tmdb_video_id UNIQUE (tmdb_video_id) ON CONFLICT REPLACE"
            + " );";

    // @formatter:on

    public static MovieDBHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = newInstance(context.getApplicationContext());
        }
        return sInstance;
    }

    private static MovieDBHelper newInstance(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return newInstancePreHoneycomb(context);
        }
        return newInstancePostHoneycomb(context);
    }


    /*
     * Pre Honeycomb.
     */
    private static MovieDBHelper newInstancePreHoneycomb(Context context) {
        return new MovieDBHelper(context);
    }

    private MovieDBHelper(Context context) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
        mContext = context;
        mOpenHelperCallbacks = new MovieDBHelperCallbacks();
    }


    /*
     * Post Honeycomb.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static MovieDBHelper newInstancePostHoneycomb(Context context) {
        return new MovieDBHelper(context, new DefaultDatabaseErrorHandler());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private MovieDBHelper(Context context, DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION, errorHandler);
        mContext = context;
        mOpenHelperCallbacks = new MovieDBHelperCallbacks();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        mOpenHelperCallbacks.onPreCreate(mContext, db);
        db.execSQL(SQL_CREATE_TABLE_LIST_POPULAR);
        db.execSQL(SQL_CREATE_TABLE_LIST_TOP_RATED);
        db.execSQL(SQL_CREATE_TABLE_MOVIE);
        db.execSQL(SQL_CREATE_TABLE_REVIEW);
        db.execSQL(SQL_CREATE_TABLE_VIDEO);
        mOpenHelperCallbacks.onPostCreate(mContext, db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            setForeignKeyConstraintsEnabled(db);
        }
        mOpenHelperCallbacks.onOpen(mContext, db);
    }

    private void setForeignKeyConstraintsEnabled(SQLiteDatabase db) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setForeignKeyConstraintsEnabledPreJellyBean(db);
        } else {
            setForeignKeyConstraintsEnabledPostJellyBean(db);
        }
    }

    private void setForeignKeyConstraintsEnabledPreJellyBean(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setForeignKeyConstraintsEnabledPostJellyBean(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mOpenHelperCallbacks.onUpgrade(mContext, db, oldVersion, newVersion);
    }
}
