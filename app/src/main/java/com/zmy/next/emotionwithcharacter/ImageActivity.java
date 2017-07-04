package com.zmy.next.emotionwithcharacter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageActivity extends AppCompatActivity {

    private SimpleDraweeView imageView;
    private Button button;
    private EditText editText;
    private FrameLayout container;
    private LinearLayout images;
    private AlertDialog alertDialog;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageView = (SimpleDraweeView) findViewById(R.id.image);
        button = (Button) findViewById(R.id.btn);
        editText = (EditText) findViewById(R.id.edit);
        container = (FrameLayout) findViewById(R.id.container);
        images = (LinearLayout) findViewById(R.id.images);

        alertDialog = new AlertDialog.Builder(this).setView(new ProgressBar(this))
                .setMessage("正在生成gif图片").create();

        final String imageUrl = getIntent().getStringExtra("url");

        final Uri uri = Uri.parse(imageUrl);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        imageView.setController(controller);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!imageUrl.endsWith(".gif")) {
                    //方案一：不可行 会把输入框也draw进去
//                    container.setDrawingCacheEnabled(true);
//                    Bitmap bitmap = container.getDrawingCache();
//                    String filePath = saveMyBitmap("temp", bitmap);
//                    container.setDrawingCacheEnabled(false);
                    //方案二：canvas draw
                    startTime = System.currentTimeMillis();
                    Bitmap bitmap1 = createWatermark(drawableToBitmap(imageView.getDrawable()), editText.getText().toString());
                    boolean success = save(bitmap1);
                    if (success) {
                        long endTime = System.currentTimeMillis();
                        editText.setVisibility(View.GONE);
                        Toast.makeText(ImageActivity.this, "合成完成 耗时 " + (endTime - startTime) + "ms", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    alertDialog.show();
                    startTime = System.currentTimeMillis();
                    dealGif();
                }
            }
        });
    }

    private void dealGif() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                ByteArrayOutputStream baos = showGif();

                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/GIFMakerDemo");
                if (!file.exists()) file.mkdir();
                final String filePath = Environment.getExternalStorageDirectory().getPath() + "/GIFMakerDemo/" + "temp" + ".gif";
                try {
                    FileOutputStream fos = new FileOutputStream(filePath);
                    baos.writeTo(fos);
                    baos.flush();
                    fos.flush();
                    baos.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Uri uri = Uri.fromFile(new File(filePath));
                        DraweeController controller = Fresco.newDraweeControllerBuilder()
                                .setUri(uri)
                                .setAutoPlayAnimations(true)
                                .build();
                        imageView.setController(controller);

                        editText.setVisibility(View.GONE);
                        long endTime = System.currentTimeMillis();
                        Toast.makeText(ImageActivity.this, "合成完成 耗时 " + (endTime - startTime) + "ms", Toast.LENGTH_SHORT).show();
                    }
                });

                alertDialog.dismiss();

            }
        }.start();
    }

    private GifImageDecoder gifDecoder;

    private ByteArrayOutputStream showGif() {
        gifDecoder = new GifImageDecoder();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnimatedGifEncoder localAnimatedGifEncoder = new AnimatedGifEncoder();
        localAnimatedGifEncoder.start(baos);//start
        localAnimatedGifEncoder.setRepeat(0);//设置生成gif的开始播放时间。0为立即开始播放
        localAnimatedGifEncoder.setDelay(0);
        try {
            gifDecoder.read(ImageActivity.this.getResources().openRawResource(R.raw.b));
            int size = gifDecoder.getFrameCount();
            Bitmap bitmap;
            for (int i = 0; i < size; i++) {
//                ImageView iv_image = new ImageView(ImageActivity.this);
//                iv_image.setPadding(5, 5, 5, 5);
//                LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(500, 500);
//                iv_image.setLayoutParams(lparams);
//                gifDecoder.getFrame(i);//231ms
                bitmap = createWatermark(gifDecoder.getFrame(i), editText.getText().toString());//299ms
                localAnimatedGifEncoder.addFrame(bitmap);


//                iv_image.setImageBitmap(bitmap);
//                images.addView(iv_image);
            }

            localAnimatedGifEncoder.finish();//finish
        } catch (Resources.NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return baos;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        Log.e("zmy", " w = " + w + " h= " + h);
        if (w <= 0 || h <= 0) {
            w = 100;
            h = 100;
        }

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }

    private boolean save(Bitmap bitmap) {
        if (bitmap == null) {
            return false;
        }
        String filePath = saveMyBitmap("temp", bitmap);

        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        Uri uri = Uri.fromFile(new File(filePath));
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        imageView.setController(controller);
        return true;
    }

    private Bitmap createWatermark(Bitmap target, String mark) {
        if (target == null) {
            return null;
        }
        int w = target.getWidth();
        int h = target.getHeight();

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Paint p = new Paint();

        // 水印的颜色
        p.setColor(Color.RED);

        // 水印的字体大小
        p.setTextSize(20);

        p.setAntiAlias(true);// 去锯齿

        canvas.drawBitmap(target, 0, 0, p);

        // 在左边的中间位置开始添加水印
        canvas.drawText(mark, w / 2, h / 2, p);

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        return bmp;
    }


    public static String saveMyBitmap(String bitName, Bitmap bitmap) {
        if (TextUtils.isEmpty(bitName) || bitmap == null) {
            return null;
        }
        File f = new File("/sdcard/" + bitName + ".png");

        try {
            f.createNewFile();
        } catch (IOException e) {
            System.out.println("在保存图片时出错：" + e.toString());
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        } catch (Exception e) {
            return "create_bitmap_error";
        }
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "/sdcard/" + bitName + ".png";
    }
}
