package com.gmail.raducaz.arduinomate.viewmodel;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.CommentEntity;
import com.gmail.raducaz.arduinomate.model.Comment;

import java.util.List;

public class CommentViewModel extends AndroidViewModel {

    private DataRepository dataRepository;
    private final LiveData<CommentEntity> mObservableComment;

    public ObservableField<CommentEntity> comment = new ObservableField<>();

    private final int mCommentId;

//    private final LiveData<List<CommentEntity>> mObservableComments;

    public CommentViewModel(@NonNull Application application, DataRepository repository,
                            final int commentId) {
        super(application);
        mCommentId = commentId;
        dataRepository = repository;
//        mObservableComments = repository.loadComments(mProductId);

        mObservableComment = repository.loadComment(mCommentId);
    }

//    /**
//     * Expose the LiveData Comments query so the UI can observe it.
//     */
//    public LiveData<List<CommentEntity>> getComments() {
//        return mObservableComments;
//    }

    public LiveData<CommentEntity> getObservableComment() {
        return mObservableComment;
    }

    public void setComment(CommentEntity comment) {
        this.comment.set(comment);
    }

    public void updateComment(CommentEntity comment)
    {
        dataRepository.updateComment(comment);
    }
    /**
     * A creator is used to inject the comment ID into the ViewModel
     * <p>
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the product ID can be passed in a public method.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final int mCommentId;

        private final DataRepository mRepository;

        public Factory(@NonNull Application application, int commentId) {
            mApplication = application;
            mCommentId = commentId;
            mRepository = ((ArduinoMateApp) application).getRepository();
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new CommentViewModel(mApplication, mRepository, mCommentId);
        }
    }
}