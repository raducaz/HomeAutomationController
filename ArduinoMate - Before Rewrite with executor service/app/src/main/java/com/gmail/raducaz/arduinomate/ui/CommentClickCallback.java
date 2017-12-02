package com.gmail.raducaz.arduinomate.ui;

import com.gmail.raducaz.arduinomate.model.Comment;
import com.gmail.raducaz.arduinomate.viewmodel.CommentViewModel;

public interface CommentClickCallback {
    void onClick(Comment comment);
}
