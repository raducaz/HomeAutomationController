package com.gmail.raducaz.arduinomate.ui;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.databinding.ProductFragmentBinding;
import com.gmail.raducaz.arduinomate.db.entity.CommentEntity;
import com.gmail.raducaz.arduinomate.db.entity.ProductEntity;
import com.gmail.raducaz.arduinomate.model.Comment;
import com.gmail.raducaz.arduinomate.model.Product;
import com.gmail.raducaz.arduinomate.network.TcpClient;
import com.gmail.raducaz.arduinomate.network.TcpServerService;
import com.gmail.raducaz.arduinomate.viewmodel.CommentViewModel;
import com.gmail.raducaz.arduinomate.viewmodel.ProductViewModel;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Observable;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ProductFragment extends Fragment {

    private static final String KEY_PRODUCT_ID = "product_id";

    private ProductFragmentBinding mBinding;

    private CommentAdapter mCommentAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.product_fragment, container, false);

        // Create and set the adapter for the RecyclerView.
        mCommentAdapter = new CommentAdapter(mCommentClickCallback);
        mBinding.commentList.setAdapter(mCommentAdapter);

        return mBinding.getRoot();
    }

    private final CommentClickCallback mCommentClickCallback = new CommentClickCallback() {
        @Override
        public void onClick(Comment comment) {

            if(comment.getText().equals("ProgressFct")) {

                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    ((MainActivity) getActivity()).show(comment);
                }
            }
            else
            {
                if(!comment.getText().equals("MonitorFct")) {
                    // Start sending command to Arduino
                    DataRepository repository = ((ArduinoMateApp) getActivity().getApplication()).getRepository();
                    ((MainActivity) getActivity()).tcpClient.stop();
                    ((MainActivity) getActivity()).tcpClient = new TcpClient("", "");
                    ((MainActivity) getActivity()).tcpClient.execute(
                            new CommentChannelInboundHandler((CommentEntity) comment, repository)
                    );
                }
                else
                {
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        ((MainActivity) getActivity()).startTcpServerService(comment);
                    }
                }
            }
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ProductViewModel.Factory factory = new ProductViewModel.Factory(
                getActivity().getApplication(), getArguments().getInt(KEY_PRODUCT_ID));

        final ProductViewModel model = ViewModelProviders.of(this, factory)
                .get(ProductViewModel.class);

        mBinding.setProductViewModel(model);

        // Test Live Data
        Button button = (Button) mBinding.getRoot().findViewById(R.id.btn_test);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View b)
            {
                ((ArduinoMateApp)getActivity().getApplication()).getDbExecutor().execute(() -> {
                    model.updateProduct();
                });
            }
        });
        // Test Live Data

        subscribeToModel(model);
    }

    private void subscribeToModel(final ProductViewModel model) {

        // Observe product data
        model.getObservableProduct().observe(this, new Observer<ProductEntity>() {
            @Override
            public void onChanged(@Nullable ProductEntity productEntity) {
                model.setProduct(productEntity);
            }
        });

        // Observe comments
        model.getComments().observe(this, new Observer<List<CommentEntity>>() {
            @Override
            public void onChanged(@Nullable List<CommentEntity> commentEntities) {
                if (commentEntities != null) {
                    mBinding.setIsLoading(false);
                    mCommentAdapter.setCommentList(commentEntities);
                } else {
                    mBinding.setIsLoading(true);
                }
            }
        });
    }

    /** Creates product fragment for specific product ID */
    public static ProductFragment forProduct(int productId) {
        ProductFragment fragment = new ProductFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }
}
