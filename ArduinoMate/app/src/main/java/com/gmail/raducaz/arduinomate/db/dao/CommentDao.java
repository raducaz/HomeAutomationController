package com.gmail.raducaz.arduinomate.db.dao;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.gmail.raducaz.arduinomate.db.entity.CommentEntity;

import java.util.List;

@Dao
public interface CommentDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(CommentEntity comment);

    @Query("SELECT * FROM comments where productId = :productId")
    LiveData<List<CommentEntity>> loadComments(int productId);

    @Query("SELECT * FROM comments where productId = :productId")
    List<CommentEntity> loadCommentsSync(int productId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CommentEntity> products);

    @Query("select * from comments where id = :commentId")
    LiveData<CommentEntity> loadComment(int commentId);

    @Query("select * from comments where id = :commentId")
    CommentEntity loadCommentSync(int commentId);
}

