package com.glovesz.avatarcam;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;

public class CaptureActivity extends Activity {
    private static final int PICK_AVATAR = 91;

    private ImageView preview;
    private TextView state;
    private Button useAvatar;
    private Bitmap avatar;

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        loadSavedAvatar();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(24));
        root.setBackgroundColor(Color.rgb(10, 10, 12));

        TextView title = new TextView(this);
        title.setText("AvatarCam");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30f);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Choose the avatar to return as this camera image.");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(15f);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.setMargins(0, dp(6), 0, dp(18));
        root.addView(subtitle, subtitleParams);

        preview = new ImageView(this);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setBackgroundColor(Color.rgb(25, 25, 29));
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(-1, 0, 1f);
        root.addView(preview, previewParams);

        state = new TextView(this);
        state.setTextColor(Color.LTGRAY);
        state.setGravity(Gravity.CENTER);
        state.setTextSize(14f);
        LinearLayout.LayoutParams stateParams = new LinearLayout.LayoutParams(-1, -2);
        stateParams.setMargins(0, dp(10), 0, dp(10));
        root.addView(state, stateParams);

        Button choose = new Button(this);
        choose.setText("Choose different avatar");
        choose.setAllCaps(false);
        choose.setOnClickListener(v -> chooseAvatar());
        root.addView(choose, new LinearLayout.LayoutParams(-1, -2));

        useAvatar = new Button(this);
        useAvatar.setText("Use this avatar");
        useAvatar.setAllCaps(false);
        useAvatar.setEnabled(false);
        useAvatar.setOnClickListener(v -> returnAvatar());
        LinearLayout.LayoutParams useParams = new LinearLayout.LayoutParams(-1, -2);
        useParams.setMargins(0, dp(8), 0, 0);
        root.addView(useAvatar, useParams);

        Button cancel = new Button(this);
        cancel.setText("Cancel");
        cancel.setAllCaps(false);
        cancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(-1, -2);
        cancelParams.setMargins(0, dp(8), 0, 0);
        root.addView(cancel, cancelParams);

        setContentView(root);
    }

    private void loadSavedAvatar() {
        avatar = AvatarStore.load(this);
        if (avatar != null) {
            preview.setImageBitmap(avatar);
            state.setText("Saved avatar ready");
            useAvatar.setEnabled(true);
        } else {
            state.setText("No avatar saved yet");
        }
    }

    private void chooseAvatar() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(i, PICK_AVATAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_AVATAR || resultCode != RESULT_OK || data == null || data.getData() == null) return;

        try {
            avatar = AvatarStore.importFromUri(this, data.getData());
            preview.setImageBitmap(avatar);
            state.setText("Avatar ready");
            useAvatar.setEnabled(true);
        } catch (Exception e) {
            Toast.makeText(this, "Could not load avatar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void returnAvatar() {
        if (avatar == null) {
            Toast.makeText(this, "Choose an avatar first.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent launchIntent = getIntent();
        Uri output = launchIntent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);

        try {
            if (output != null) {
                try (OutputStream stream = getContentResolver().openOutputStream(output)) {
                    if (stream == null || !avatar.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                        throw new IllegalStateException("Could not write camera result.");
                    }
                }
                setResult(RESULT_OK);
            } else {
                Bitmap thumbnail = makeThumbnail(avatar);
                Intent result = new Intent();
                result.putExtra("data", thumbnail);
                setResult(RESULT_OK, result);
            }
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Could not return avatar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
        }
    }

    private Bitmap makeThumbnail(Bitmap source) {
        int max = 512;
        int width = source.getWidth();
        int height = source.getHeight();
        if (Math.max(width, height) <= max) return source;

        float scale = (float) max / (float) Math.max(width, height);
        return Bitmap.createScaledBitmap(
                source,
                Math.max(1, Math.round(width * scale)),
                Math.max(1, Math.round(height * scale)),
                true
        );
    }
}
