package com.iqra.chinese.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AttemptDao_Impl implements AttemptDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AttemptRecord> __insertionAdapterOfAttemptRecord;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfClear;

  public AttemptDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAttemptRecord = new EntityInsertionAdapter<AttemptRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `attempts` (`itemId`,`itemType`,`level`,`pass`,`fail`,`lastTs`,`history`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AttemptRecord entity) {
        if (entity.getItemId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getItemId());
        }
        if (entity.getItemType() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getItemType());
        }
        statement.bindLong(3, entity.getLevel());
        statement.bindLong(4, entity.getPass());
        statement.bindLong(5, entity.getFail());
        statement.bindLong(6, entity.getLastTs());
        final String _tmp = __converters.fromList(entity.getHistory());
        if (_tmp == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, _tmp);
        }
      }
    };
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM attempts";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final AttemptRecord r, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAttemptRecord.insert(r);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clear(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClear.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClear.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object get(final String id, final Continuation<? super AttemptRecord> $completion) {
    final String _sql = "SELECT * FROM attempts WHERE itemId=?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (id == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, id);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AttemptRecord>() {
      @Override
      @Nullable
      public AttemptRecord call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "itemId");
          final int _cursorIndexOfItemType = CursorUtil.getColumnIndexOrThrow(_cursor, "itemType");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfPass = CursorUtil.getColumnIndexOrThrow(_cursor, "pass");
          final int _cursorIndexOfFail = CursorUtil.getColumnIndexOrThrow(_cursor, "fail");
          final int _cursorIndexOfLastTs = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTs");
          final int _cursorIndexOfHistory = CursorUtil.getColumnIndexOrThrow(_cursor, "history");
          final AttemptRecord _result;
          if (_cursor.moveToFirst()) {
            final String _tmpItemId;
            if (_cursor.isNull(_cursorIndexOfItemId)) {
              _tmpItemId = null;
            } else {
              _tmpItemId = _cursor.getString(_cursorIndexOfItemId);
            }
            final String _tmpItemType;
            if (_cursor.isNull(_cursorIndexOfItemType)) {
              _tmpItemType = null;
            } else {
              _tmpItemType = _cursor.getString(_cursorIndexOfItemType);
            }
            final int _tmpLevel;
            _tmpLevel = _cursor.getInt(_cursorIndexOfLevel);
            final int _tmpPass;
            _tmpPass = _cursor.getInt(_cursorIndexOfPass);
            final int _tmpFail;
            _tmpFail = _cursor.getInt(_cursorIndexOfFail);
            final long _tmpLastTs;
            _tmpLastTs = _cursor.getLong(_cursorIndexOfLastTs);
            final List<AttemptEntry> _tmpHistory;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfHistory)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfHistory);
            }
            _tmpHistory = __converters.toList(_tmp);
            _result = new AttemptRecord(_tmpItemId,_tmpItemType,_tmpLevel,_tmpPass,_tmpFail,_tmpLastTs,_tmpHistory);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AttemptRecord>> all() {
    final String _sql = "SELECT * FROM attempts";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"attempts"}, new Callable<List<AttemptRecord>>() {
      @Override
      @NonNull
      public List<AttemptRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "itemId");
          final int _cursorIndexOfItemType = CursorUtil.getColumnIndexOrThrow(_cursor, "itemType");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfPass = CursorUtil.getColumnIndexOrThrow(_cursor, "pass");
          final int _cursorIndexOfFail = CursorUtil.getColumnIndexOrThrow(_cursor, "fail");
          final int _cursorIndexOfLastTs = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTs");
          final int _cursorIndexOfHistory = CursorUtil.getColumnIndexOrThrow(_cursor, "history");
          final List<AttemptRecord> _result = new ArrayList<AttemptRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AttemptRecord _item;
            final String _tmpItemId;
            if (_cursor.isNull(_cursorIndexOfItemId)) {
              _tmpItemId = null;
            } else {
              _tmpItemId = _cursor.getString(_cursorIndexOfItemId);
            }
            final String _tmpItemType;
            if (_cursor.isNull(_cursorIndexOfItemType)) {
              _tmpItemType = null;
            } else {
              _tmpItemType = _cursor.getString(_cursorIndexOfItemType);
            }
            final int _tmpLevel;
            _tmpLevel = _cursor.getInt(_cursorIndexOfLevel);
            final int _tmpPass;
            _tmpPass = _cursor.getInt(_cursorIndexOfPass);
            final int _tmpFail;
            _tmpFail = _cursor.getInt(_cursorIndexOfFail);
            final long _tmpLastTs;
            _tmpLastTs = _cursor.getLong(_cursorIndexOfLastTs);
            final List<AttemptEntry> _tmpHistory;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfHistory)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfHistory);
            }
            _tmpHistory = __converters.toList(_tmp);
            _item = new AttemptRecord(_tmpItemId,_tmpItemType,_tmpLevel,_tmpPass,_tmpFail,_tmpLastTs,_tmpHistory);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object allOnce(final Continuation<? super List<AttemptRecord>> $completion) {
    final String _sql = "SELECT * FROM attempts";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AttemptRecord>>() {
      @Override
      @NonNull
      public List<AttemptRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "itemId");
          final int _cursorIndexOfItemType = CursorUtil.getColumnIndexOrThrow(_cursor, "itemType");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfPass = CursorUtil.getColumnIndexOrThrow(_cursor, "pass");
          final int _cursorIndexOfFail = CursorUtil.getColumnIndexOrThrow(_cursor, "fail");
          final int _cursorIndexOfLastTs = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTs");
          final int _cursorIndexOfHistory = CursorUtil.getColumnIndexOrThrow(_cursor, "history");
          final List<AttemptRecord> _result = new ArrayList<AttemptRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AttemptRecord _item;
            final String _tmpItemId;
            if (_cursor.isNull(_cursorIndexOfItemId)) {
              _tmpItemId = null;
            } else {
              _tmpItemId = _cursor.getString(_cursorIndexOfItemId);
            }
            final String _tmpItemType;
            if (_cursor.isNull(_cursorIndexOfItemType)) {
              _tmpItemType = null;
            } else {
              _tmpItemType = _cursor.getString(_cursorIndexOfItemType);
            }
            final int _tmpLevel;
            _tmpLevel = _cursor.getInt(_cursorIndexOfLevel);
            final int _tmpPass;
            _tmpPass = _cursor.getInt(_cursorIndexOfPass);
            final int _tmpFail;
            _tmpFail = _cursor.getInt(_cursorIndexOfFail);
            final long _tmpLastTs;
            _tmpLastTs = _cursor.getLong(_cursorIndexOfLastTs);
            final List<AttemptEntry> _tmpHistory;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfHistory)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfHistory);
            }
            _tmpHistory = __converters.toList(_tmp);
            _item = new AttemptRecord(_tmpItemId,_tmpItemType,_tmpLevel,_tmpPass,_tmpFail,_tmpLastTs,_tmpHistory);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AttemptRecord>> forLevel(final int l) {
    final String _sql = "SELECT * FROM attempts WHERE level=? AND itemType='word'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, l);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"attempts"}, new Callable<List<AttemptRecord>>() {
      @Override
      @NonNull
      public List<AttemptRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "itemId");
          final int _cursorIndexOfItemType = CursorUtil.getColumnIndexOrThrow(_cursor, "itemType");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfPass = CursorUtil.getColumnIndexOrThrow(_cursor, "pass");
          final int _cursorIndexOfFail = CursorUtil.getColumnIndexOrThrow(_cursor, "fail");
          final int _cursorIndexOfLastTs = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTs");
          final int _cursorIndexOfHistory = CursorUtil.getColumnIndexOrThrow(_cursor, "history");
          final List<AttemptRecord> _result = new ArrayList<AttemptRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AttemptRecord _item;
            final String _tmpItemId;
            if (_cursor.isNull(_cursorIndexOfItemId)) {
              _tmpItemId = null;
            } else {
              _tmpItemId = _cursor.getString(_cursorIndexOfItemId);
            }
            final String _tmpItemType;
            if (_cursor.isNull(_cursorIndexOfItemType)) {
              _tmpItemType = null;
            } else {
              _tmpItemType = _cursor.getString(_cursorIndexOfItemType);
            }
            final int _tmpLevel;
            _tmpLevel = _cursor.getInt(_cursorIndexOfLevel);
            final int _tmpPass;
            _tmpPass = _cursor.getInt(_cursorIndexOfPass);
            final int _tmpFail;
            _tmpFail = _cursor.getInt(_cursorIndexOfFail);
            final long _tmpLastTs;
            _tmpLastTs = _cursor.getLong(_cursorIndexOfLastTs);
            final List<AttemptEntry> _tmpHistory;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfHistory)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfHistory);
            }
            _tmpHistory = __converters.toList(_tmp);
            _item = new AttemptRecord(_tmpItemId,_tmpItemType,_tmpLevel,_tmpPass,_tmpFail,_tmpLastTs,_tmpHistory);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> masteredCount() {
    final String _sql = "SELECT COUNT(*) FROM attempts WHERE itemType='word' AND pass>0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"attempts"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
