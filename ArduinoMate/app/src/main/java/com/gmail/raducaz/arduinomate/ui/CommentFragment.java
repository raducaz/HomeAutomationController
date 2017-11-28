package com.gmail.raducaz.arduinomate.ui;

        import android.arch.lifecycle.LifecycleFragment;
        import android.arch.lifecycle.Observer;
        import android.arch.lifecycle.ViewModelProviders;
        import android.databinding.DataBindingUtil;
        import android.os.Bundle;
        import android.support.annotation.Nullable;
        import android.support.v4.app.Fragment;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;

        import com.gmail.raducaz.arduinomate.R;
        import com.gmail.raducaz.arduinomate.db.entity.CommentEntity;
        import com.gmail.raducaz.arduinomate.db.entity.ProductEntity;
        import com.gmail.raducaz.arduinomate.model.Comment;
        import com.gmail.raducaz.arduinomate.databinding.CommentFragmentBinding;
        import com.gmail.raducaz.arduinomate.viewmodel.CommentViewModel;

        import java.util.List;

public class CommentFragment extends Fragment {

    private static final String KEY_COMMENT_ID = "comment_id";

    private CommentFragmentBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
                mBinding = DataBindingUtil.inflate(inflater, R.layout.product_fragment, container, false);

//        // Create and set the adapter for the RecyclerView.
//        mCommentAdapter = new CommentAdapter(mCommentClickCallback);
//        mBinding.commentList.setAdapter(mCommentAdapter);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        CommentViewModel.Factory factory = new CommentViewModel.Factory(
                getActivity().getApplication(), getArguments().getInt(KEY_COMMENT_ID));

        final CommentViewModel model = ViewModelProviders.of(this, factory)
                .get(CommentViewModel.class);

        mBinding.setCommentViewModel(model);

        subscribeToModel(model);
    }

    private void subscribeToModel(final CommentViewModel model) {

        // Observe product data
        model.getObservableComment().observe(this, new Observer<CommentEntity>() {
            @Override
            public void onChanged(@Nullable CommentEntity commentEntity) {
                model.setComment(commentEntity);
            }
        });


    }

    /** Creates comment fragment for specific comment ID */
    public static CommentFragment forComment(int commentId) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_COMMENT_ID, commentId);
        fragment.setArguments(args);
        return fragment;
    }
}
