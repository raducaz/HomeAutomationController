package com.gmail.raducaz.arduinomate.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.model.Comment;
import com.gmail.raducaz.arduinomate.model.Product;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Add product list fragment if this is first creation
        if (savedInstanceState == null) {
            ProductListFragment fragment = new ProductListFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, ProductListFragment.TAG).commit();
        }
    }

    /** Shows the product detail fragment */
    public void show(Product product) {

        ProductFragment productFragment = ProductFragment.forProduct(product.getId());

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("product")
                .replace(R.id.fragment_container,
                        productFragment, null).commit();
    }

    /** Shows the comment detail fragment */
    public void show(Comment comment) {

        CommentFragment commentFragment = CommentFragment.forComment(comment.getId());

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("comment")
                .replace(R.id.fragment_container,
                        commentFragment, null).commit();
    }
}













/// OLD CODE - not used
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.widget.GridView;
//
//import com.gmail.raducaz.arduinomate.ConfigurationGridAdapter;
//import com.gmail.raducaz.arduinomate.R;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MainActivity extends AppCompatActivity {
//
//    GridView gridView;
//    ConfigurationGridAdapter adapter;
//    List<String> list;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main_old);
//
//        list = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            list.add("Test " + i);
//        }
//        gridView = findViewById(R.id.configurationGridView);
//        adapter = new ConfigurationGridAdapter(this, list);
//        gridView.setAdapter(adapter);
//    }
//}
