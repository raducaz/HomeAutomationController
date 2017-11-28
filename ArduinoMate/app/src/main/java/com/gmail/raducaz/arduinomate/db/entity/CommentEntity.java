package com.gmail.raducaz.arduinomate.db.entity;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.gmail.raducaz.arduinomate.model.Comment;

import java.util.Date;

@Entity(tableName = "comments",
        foreignKeys = {
                @ForeignKey(entity = ProductEntity.class,
                        parentColumns = "id",
                        childColumns = "productId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "productId")
        })
public class CommentEntity implements Comment {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int productId;
    private String text;
    private String log;
    private Date postedAt;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    @Override
    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Date getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(Date postedAt) {
        this.postedAt = postedAt;
    }

    public CommentEntity() {
    }

    public CommentEntity(int id, int productId, String text, String log, Date postedAt) {
        this.id = id;
        this.productId = productId;
        this.text = text;
        this.log = log;
        this.postedAt = postedAt;
    }
}