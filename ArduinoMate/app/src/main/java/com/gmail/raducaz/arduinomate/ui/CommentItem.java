package com.gmail.raducaz.arduinomate.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.databinding.CommentItemBinding;
import com.gmail.raducaz.arduinomate.model.Comment;


public class CommentItem extends Fragment {

    private CommentItemBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.comment_item, container, false);
        mBinding.setCallback(mCommentClickCallback);
        return mBinding.getRoot();
    }

    private final CommentClickCallback mCommentClickCallback = new CommentClickCallback() {
        @Override
        public void onClick(Comment comment) {

        }
    };

}
