package com.example.prj_crudkotlin.roomDB;

import android.database.Cursor;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
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
public final class PessoaDao_Impl implements PessoaDao {
  private final RoomDatabase __db;

  private final EntityDeletionOrUpdateAdapter<Pessoa> __deletionAdapterOfPessoa;

  private final EntityUpsertionAdapter<Pessoa> __upsertionAdapterOfPessoa;

  public PessoaDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__deletionAdapterOfPessoa = new EntityDeletionOrUpdateAdapter<Pessoa>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `Pessoa` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Pessoa value) {
        stmt.bindLong(1, value.getId());
      }
    };
    this.__upsertionAdapterOfPessoa = new EntityUpsertionAdapter<Pessoa>(new EntityInsertionAdapter<Pessoa>(__db) {
      @Override
      public String createQuery() {
        return "INSERT INTO `Pessoa` (`name`,`telefone`,`id`) VALUES (?,?,nullif(?, 0))";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Pessoa value) {
        if (value.getName() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getName());
        }
        if (value.getTelefone() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getTelefone());
        }
        stmt.bindLong(3, value.getId());
      }
    }, new EntityDeletionOrUpdateAdapter<Pessoa>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE `Pessoa` SET `name` = ?,`telefone` = ?,`id` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Pessoa value) {
        if (value.getName() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getName());
        }
        if (value.getTelefone() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getTelefone());
        }
        stmt.bindLong(3, value.getId());
        stmt.bindLong(4, value.getId());
      }
    });
  }

  @Override
  public Object deletePessoa(final Pessoa pessoa, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPessoa.handle(pessoa);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object upsertPessoa(final Pessoa pessoa, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfPessoa.upsert(pessoa);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Flow<List<Pessoa>> getAllPessoa() {
    final String _sql = "SELECT * FROM Pessoa";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[]{"Pessoa"}, new Callable<List<Pessoa>>() {
      @Override
      public List<Pessoa> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfTelefone = CursorUtil.getColumnIndexOrThrow(_cursor, "telefone");
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final List<Pessoa> _result = new ArrayList<Pessoa>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Pessoa _item;
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpTelefone;
            if (_cursor.isNull(_cursorIndexOfTelefone)) {
              _tmpTelefone = null;
            } else {
              _tmpTelefone = _cursor.getString(_cursorIndexOfTelefone);
            }
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            _item = new Pessoa(_tmpName,_tmpTelefone,_tmpId);
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

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
