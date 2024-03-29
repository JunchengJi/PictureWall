package com.trio.picturewall.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trio.picturewall.Http.Api;
import com.trio.picturewall.R;
import com.trio.picturewall.adapter.CommentAdapter;
import com.trio.picturewall.entity.Comment;
import com.trio.picturewall.entity.CommentRecords;
import com.trio.picturewall.entity.MyPosts;
import com.trio.picturewall.information.LoginData;
import com.trio.picturewall.responseBody.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    public static MyPosts post;
    TextView user_name;
    TextView post_title;
    TextView post_context;
    TextView com_num;

    EditText com_edit_text;
    ImageView focus;
    ImageView photo;
    ImageView cancel;
    ImageView btn_like;
    ImageView btn_collect;
    ImageView btn_comment;
    ImageView forward_image;
    ImageView next_image;
    CommentAdapter commentAdapter;
    ImageView btn_close;
    ImageView com_post;
    private Dialog com_dialog;
    private RecyclerView comment_list; //评论列表
    private SwipeRefreshLayout swipe_comment;
    private List<Comment> comments = new ArrayList<>(); //评论数据list
    private int com_count = 0;

    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //view = inflater.inflate(R.layout.fragment_find, container, false);
        //swipe_comment = findViewById(R.id.swipe_comment);
//        getdetail();
        getComment();
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        initView();
        initData();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.focus:
                if (!post.getHasFocus()) {//detail.getHasFocus() == false：未关注->已关注
                    hasfocus();
                    focus.setImageResource(R.mipmap.focus);
                    post.setHasFocus(true);
                } else {
                    cancelfocus();
                    focus.setImageResource(R.mipmap.unfocus);
                    post.setHasFocus(false);
                }
                break;

//            case R.id.image_detail://图片监听
//                //
//                break;
            case R.id.btn_like:
                if (!post.getHasLike()) {//detail.getHasFocus() == false：未关注->已关注
                    like();
                    btn_like.setImageResource(R.mipmap.good_fill);
                    post.setHasLike(true);
                } else {
                    cancelLike();
                    btn_like.setImageResource(R.mipmap.good);
                    post.setHasLike(false);
                }
                break;
            case R.id.btn_collect:
                if (!post.getHasCollect()) {//detail.getHasFocus() == false：未关注->已关注
                    collect();
                    btn_collect.setImageResource(R.mipmap.collected);
                    post.setHasCollect(true);
                } else {
                    cancelCollect();
                    btn_collect.setImageResource(R.mipmap.collect);
                    post.setHasCollect(false);
                }
                break;
            case R.id.btn_comment:
                showCommentDialog();
                break;
            case R.id.btn_close:
                com_dialog.dismiss();
                break;
            case R.id.forward_image: {
                if (post.getImageUrlList().length != 0 && i > 0) {//解决没有图片闪退
                    Glide.with(this).load(post.getImageUrlList()[--i]).into(photo);
                } else if (i == 0) {//到第一张图片后再向左，则加载最后一张图片
                    i = post.getImageUrlList().length - 1;
                    Glide.with(this).load(post.getImageUrlList()[i]).into(photo);
                }
                break;
            }
            case R.id.next_image: {
                if (post.getImageUrlList().length != 0 && i < post.getImageUrlList().length - 1) {//解决没有图片闪退
                    Glide.with(this).load(post.getImageUrlList()[++i]).into(photo);
                } else if (i == post.getImageUrlList().length - 1) {//到最后一张图片后再向右，则加载第一张图片
                    i = 0;
                    Glide.with(this).load(post.getImageUrlList()[i]).into(photo);
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.image_detail://图片监听
                Log.d("Detail", "onLongClick");
                showListDialog();
                break;
            default:
                break;
        }
        return true;
    }

    public void initView() {
        user_name = findViewById(R.id.user_name);
        post_title = findViewById(R.id.post_title);
        post_context = findViewById(R.id.post_context);
        focus = findViewById(R.id.focus);
        photo = findViewById(R.id.image_detail);
        cancel = findViewById(R.id.cancel);
        btn_like = findViewById(R.id.btn_like);
        btn_collect = findViewById(R.id.btn_collect);
        btn_comment = findViewById(R.id.btn_comment);
        forward_image = findViewById(R.id.forward_image);
        next_image = findViewById(R.id.next_image);
        focus.setOnClickListener(this);
//        photo.setOnClickListener(this);
        photo.setOnLongClickListener(this);
        cancel.setOnClickListener(this);
        btn_like.setOnClickListener(this);
        btn_collect.setOnClickListener(this);
        btn_comment.setOnClickListener(this);
        forward_image.setOnClickListener(this);
        next_image.setOnClickListener(this);
    }

    public void initData() {
        if (!post.getHasFocus()) {//detail.getHasFocus() == false：未关注->已关注
            focus.setImageResource(R.mipmap.unfocus);
        } else {
            focus.setImageResource(R.mipmap.focus);
        }
        if (!post.getHasLike()) {//detail.getHasFocus() == false：未关注->已关注
            btn_like.setImageResource(R.mipmap.good);
        } else {
            btn_like.setImageResource(R.mipmap.good_fill);
        }
        if (!post.getHasCollect()) {//detail.getHasFocus() == false：未关注->已关注
            btn_collect.setImageResource(R.mipmap.collect);
        } else {
            btn_collect.setImageResource(R.mipmap.collected);
        }
        user_name.setText(post.getUsername());
        post_title.setText(post.getTitle());
        post_context.setText(post.getContent());
        if (post.getImageUrlList().length != 0) {//解决没有图片闪退
            Glide.with(this).load(post.getImageUrlList()[0]).into(photo);
        }
    }

    public void hasfocus() {
        // url路径
        String url = "http://47.107.52.7:88/member/photo/focus?focusUserId="
                + post.getpUserId() + "&userId="
                + LoginData.loginUser.getId();

        // 请求头
        Headers headers = new Headers.Builder()
                .add("Accept", "application/json, text/plain, */*")
                .add("appId", Api.appId)
                .add("appSecret", Api.appSecret)
                .add("Content-Type", "application/json")
                .build();
        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
        //请求组合创建
        Request request = new Request.Builder()
                .url(url)
                // 将请求头加至请求中
                .headers(headers)
                .post(RequestBody.create(MEDIA_TYPE_JSON, ""))
                .build();

        try {
            OkHttpClient client = new OkHttpClient();
            //发起请求，传入callback进行回调
            client.newCall(request).enqueue(ResponseBody.callback);
        } catch (NetworkOnMainThreadException ex) {
            ex.printStackTrace();
        }

    }

    public void cancelfocus() {
        // url路径
        String url = "http://47.107.52.7:88/member/photo/focus/cancel?focusUserId="
                + post.getpUserId() + "&userId="
                + LoginData.loginUser.getId();

        // 请求头
        Headers headers = new Headers.Builder()
                .add("Accept", "application/json, text/plain, */*")
                .add("appId", Api.appId)
                .add("appSecret", Api.appSecret)
                .add("Content-Type", "application/json")
                .build();
        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
        //请求组合创建
        Request request = new Request.Builder()
                .url(url)
                // 将请求头加至请求中
                .headers(headers)
                .post(RequestBody.create(MEDIA_TYPE_JSON, ""))
                .build();
        try {
            OkHttpClient client = new OkHttpClient();
            //发起请求，传入callback进行回调
            client.newCall(request).enqueue(ResponseBody.callback);
        } catch (NetworkOnMainThreadException ex) {
            ex.printStackTrace();
        }
    }

    private void collect() {
        // url路径
        String url = "http://47.107.52.7:88/member/photo/collect?" +
                "shareId=" + post.getId() +
                "&userId=" + LoginData.loginUser.getId();

        // 请求头
        Headers headers = new Headers.Builder()
                .add("appId", Api.appId)
                .add("appSecret", Api.appSecret)
                .add("Accept", "application/json, text/plain, */*")
                .build();

        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
        //请求组合创建
        Request request = new Request.Builder()
                .url(url)
                // 将请求头加至请求中
                .headers(headers)
                .post(RequestBody.create(MEDIA_TYPE_JSON, ""))
                .build();
        try {
            OkHttpClient client = new OkHttpClient();
            //发起请求，传入callback进行回调
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    //TODO 请求失败处理
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    //TODO 请求成功处理
                    Type jsonType = new TypeToken<ResponseBody<Object>>() {
                    }.getType();
                    // 获取响应体的json串
                    String body = Objects.requireNonNull(response.body()).string();
                    Log.d("info", body);
                    // 解析json串到自己封装的状态
                    ResponseBody<Object> dataResponseBody = new Gson().fromJson(body, jsonType);
                    Log.d("info", dataResponseBody.toString());
                }
            });
        } catch (NetworkOnMainThreadException ex) {
            ex.printStackTrace();
        }
    }

    private void cancelCollect() {
        // url路径
        String url = "http://47.107.52.7:88/member/photo/collect/cancel?" +
                "collectId=" + post.getCollectId();

        // 请求头
        Headers headers = new Headers.Builder()
                .add("appId", Api.appId)
                .add("appSecret", Api.appSecret)
                .add("Accept", "application/json, text/plain, */*")
                .build();

        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

        //请求组合创建
        Request request = new Request.Builder()
                .url(url)
                // 将请求头加至请求中
                .headers(headers)
                .post(RequestBody.create(MEDIA_TYPE_JSON, ""))
                .build();
        try {
            OkHttpClient client = new OkHttpClient();
            //发起请求，传入callback进行回调
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    //TODO 请求失败处理
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    //TODO 请求成功处理
                    Type jsonType = new TypeToken<ResponseBody<Object>>() {
                    }.getType();
                    // 获取响应体的json串
                    String body = Objects.requireNonNull(response.body()).string();
                    Log.d("info", body);
                    // 解析json串到自己封装的状态
                    ResponseBody<Object> dataResponseBody = new Gson().fromJson(body, jsonType);
                    Log.d("info", dataResponseBody.toString());
                }
            });
        } catch (NetworkOnMainThreadException ex) {
            ex.printStackTrace();
        }
    }

    private void like() {
        // url路径
        String url = "http://47.107.52.7:88/member/photo/like?" +
                "shareId=" + post.getId() +
                "&userId=" + LoginData.loginUser.getId();

        // 请求头
        Headers headers = new Headers.Builder()
                .add("appId", Api.appId)
                .add("appSecret", Api.appSecret)
                .add("Accept", "application/json, text/plain, */*")
                .build();


        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

        //请求组合创建
        Request request = new Request.Builder()
                .url(url)
                // 将请求头加至请求中
                .headers(headers)
                .post(RequestBody.create(MEDIA_TYPE_JSON, ""))
                .build();
        try {
            OkHttpClient client = new OkHttpClient();
            //发起请求，传入callback进行回调
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    //TODO 请求失败处理
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    //TODO 请求成功处理
                    Type jsonType = new TypeToken<ResponseBody<Object>>() {
                    }.getType();
                    // 获取响应体的json串
                    String body = Objects.requireNonNull(response.body()).string();
                    Log.d("info", body);
                    // 解析json串到自己封装的状态
                    ResponseBody<Object> dataResponseBody = new Gson().fromJson(body, jsonType);
                    Log.d("info", dataResponseBody.toString());
                }
            });
        } catch (NetworkOnMainThreadException ex) {
            ex.printStackTrace();
        }
    }

    private void cancelLike() {
        // url路径
        String url = "http://47.107.52.7:88/member/photo/like/cancel?likeId=" + post.getLikeId();
        // 请求头
        Headers headers = new Headers.Builder()
                .add("appId", Api.appId)
                .add("appSecret", Api.appSecret)
                .add("Accept", "application/json, text/plain, */*")
                .build();


        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

        //请求组合创建
        Request request = new Request.Builder()
                .url(url)
                // 将请求头加至请求中
                .headers(headers)
                .post(RequestBody.create(MEDIA_TYPE_JSON, ""))
                .build();
        try {
            OkHttpClient client = new OkHttpClient();
            //发起请求，传入callback进行回调
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    //TODO 请求失败处理
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    //TODO 请求成功处理
                    Type jsonType = new TypeToken<ResponseBody<Object>>() {
                    }.getType();
                    // 获取响应体的json串
                    String body = Objects.requireNonNull(response.body()).string();
                    Log.d("info", body);
                    // 解析json串到自己封装的状态
                    ResponseBody<Object> dataResponseBody = new Gson().fromJson(body, jsonType);
                    Log.d("info", dataResponseBody.toString());
                }
            });
        } catch (NetworkOnMainThreadException ex) {
            ex.printStackTrace();
        }
    }

    private void getComment() {
        // url路径
        String url = "http://47.107.52.7:88/member/photo/comment/first?current=1&shareId=" +
                post.getId() +
                "&size=32";

        // 请求头
        Headers headers = new Headers.Builder()
                .add("appId", Api.appId)
                .add("appSecret", Api.appSecret)
                .add("Accept", "application/json, text/plain, */*")
                .build();

        //请求组合创建
        Request request = new Request.Builder()
                .url(url)
                // 将请求头加至请求中
                .headers(headers)
                .get()
                .build();
        try {
            OkHttpClient client = new OkHttpClient();
            //发起请求，传入callback进行回调
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    //TODO 请求失败处理
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    //TODO 请求成功处理
                    Type jsonType = new TypeToken<ResponseBody<CommentRecords>>() {
                    }.getType();
                    // 获取响应体的json串
                    String body = Objects.requireNonNull(response.body()).string();
                    Log.d("CommentRecords", body);
                    // 解析json串到自己封装的状态
                    ResponseBody<CommentRecords> dataResponseBody = new Gson().fromJson(body, jsonType);
                    Log.d("CommentRecords", dataResponseBody.toString());
                    comments = dataResponseBody.getData().getRecords();
                    com_count = dataResponseBody.getData().getRecords().size();
                    Log.d("comments", comments.toString());
                }
            });
        } catch (NetworkOnMainThreadException ex) {
            ex.printStackTrace();
        }
    }


    //评论弹窗
    private void showCommentDialog() {
        //初始化弹窗
        com_dialog = new Dialog(this, R.style.Comment_Dialog_Style);
        //设置弹窗布局
        View com_view = View.inflate(this, R.layout.dialog_comment, null);
        //设置弹窗padding为0，可宽度沾满屏幕
        Window window = com_dialog.getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        //初始化弹窗大小、位置、弹出关闭动画
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        lp.windowAnimations = R.style.share_animation;
        //展示弹窗
        com_dialog.show();
        //添加弹窗view到主View
        com_dialog.setContentView(com_view, lp);
        // 设置点击对话框外部是否关闭对话框
        com_dialog.setCanceledOnTouchOutside(true);
        //绑定view
        comment_list = com_view.findViewById(R.id.comment_list);
        btn_close = com_view.findViewById(R.id.btn_close);
        com_post = com_view.findViewById(R.id.com_post);
        com_num = com_view.findViewById(R.id.com_num);
        swipe_comment = com_view.findViewById(R.id.swipe_comment);
        btn_close.setOnClickListener(this);
        com_edit_text = com_view.findViewById(R.id.com_edit_text);
        com_post.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                System.out.println(com_edit_text.getText().toString());
                addComment(com_edit_text.getText().toString(), post.getId(),
                        LoginData.loginUser.getId(), LoginData.loginUser.getUsername());
                com_edit_text.setText("");//置空输入框

            }
        });
        //开启UI线程更新UI
        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                //设置布局管理
                comment_list.setLayoutManager(new StaggeredGridLayoutManager(1, RecyclerView.VERTICAL) {
                    @Override
                    public boolean canScrollVertically() {
                        return true;
                    }
                });
//                //设置适配器
                commentAdapter = new CommentAdapter(DetailActivity.this, comments);
                comment_list.setAdapter(commentAdapter);
                com_num.setText("" + com_count);
            }
        });

        swipe_comment.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();

                //设置适配器
                commentAdapter = new CommentAdapter(DetailActivity.this, comments);
                comment_list.setAdapter(commentAdapter);
                comment_list.setLayoutManager(new StaggeredGridLayoutManager(1, RecyclerView.VERTICAL));
                com_num.setText("" + com_count);
                //在获取数据完成后设置刷新状态为false
                //isRefreshing() 是否是处于刷新状态
                if (swipe_comment.isRefreshing()) {
                    swipe_comment.setRefreshing(false);
                }
            }
        });


    }

    private void addComment(String content, int sahreID, String userId, String userName) {
        // url路径
        String url = "http://47.107.52.7:88/member/photo/comment/first";

        // 请求头
        Headers headers = new Headers.Builder()
                .add("appId", Api.appId)
                .add("appSecret", Api.appSecret)
                .add("Accept", "application/json, text/plain, */*")
                .build();

        // 请求体
        // PS.用户也可以选择自定义一个实体类，然后使用类似fastjson的工具获取json串
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("content", content);
        bodyMap.put("shareId", sahreID);
        bodyMap.put("userId", userId);
        bodyMap.put("userName", userName);
        // 将Map转换为字符串类型加入请求体中
        String body = new Gson().toJson(bodyMap);

        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

        //请求组合创建
        Request request = new Request.Builder()
                .url(url)
                // 将请求头加至请求中
                .headers(headers)
                .post(RequestBody.create(MEDIA_TYPE_JSON, body))
                .build();
        try {
            OkHttpClient client = new OkHttpClient();
            //发起请求，传入callback进行回调
            client.newCall(request).enqueue(ResponseBody.callback);
        } catch (NetworkOnMainThreadException ex) {
            ex.printStackTrace();
        }
    }

    private void refreshData() {
        getComment();
    }

    /**
     * 普通列表dialog
     */
    private void showListDialog() {
        final String[] listItems = new String[]{"保存当前图片", "保存所有图片"};

        AlertDialog.Builder listDialog = new AlertDialog.Builder(DetailActivity.this);
        listDialog.setTitle("保存图片");
        listDialog.setIcon(R.mipmap.seraphine);

    /*
        设置item 不能用setMessage()
        用setItems
        items : listItems[] -> 列表项数组
        listener -> 回调接口
    */
        listDialog.setItems(listItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0://保存当前图片
                        Log.v("Save Image :", listItems[which]);
                        new Thread() {
                            public void run() {
                                Log.e("图片", "开始下载");
                                try {
                                    Bitmap myBitmap = Glide.with(DetailActivity.this)
                                            .asBitmap()
                                            .skipMemoryCache(true)//跳过内存缓存
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)//不缓冲disk硬盘中
                                            .load(post.getImageUrlList()[i])
                                            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                                    Bitmap bitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight());
                                    Log.e("图片", bitmap.toString());
                                    saveImageToGallery(bitmap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }.start();
                        break;
                    case 1://保存所有图片
                        Log.v("Save Image :", listItems[which]);
                        new Thread() {
                            public void run() {
                                Log.e("图片", "开始下载");
                                try {
                                    for (int index = 0; index < post.getImageUrlList().length; index++) {
                                        Bitmap myBitmap = Glide.with(DetailActivity.this)
                                                .asBitmap()
                                                .skipMemoryCache(true)//跳过内存缓存
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)//不缓冲disk硬盘中
                                                .load(post.getImageUrlList()[index])
                                                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                                        Bitmap bitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight());
                                        Log.e("图片", bitmap.toString());
                                        saveImageToGallery(bitmap);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }.start();
                        break;
                }
            }
        });

        //设置按钮
        listDialog.setPositiveButton(getString(R.string.dialog_btn_confirm_text)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        listDialog.create().show();
    }

    public void saveImageToGallery(Bitmap bmp) {
        if (bmp == null) {
            Log.e("TAG", "bitmap---为空");
            return;
        }
        String galleryPath = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera" + File.separator;
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(galleryPath, fileName);
        try {
            if (!Objects.requireNonNull(file.getParentFile()).exists()) {
                if (!file.getParentFile().mkdirs()) {
                    Log.e("图片", "创建文件失败");
                }
            }
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();
            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            DetailActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                Log.e("TAG", "图片保存成功 保存在:" + file.getPath());
            } else {
                Log.e("TAG", "图片保存失败");
            }
        } catch (IOException e) {
            Log.e("TAG", "保存图片找不到文件夹");
            e.printStackTrace();
        }
    }
}
