package com.glovesz.avatarcam;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int PICK_IMAGE = 41;
    private static final int CAMERA_PERMISSION = 42;
    private static final int TEST_CAMERA = 43;

    private ImageView preview;
    private TextView avatarState;
    private Button testChooser;

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private TextView text(String value, float size, int color) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        return v;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setMinHeight(dp(52));
        return b;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
        Bitmap saved = AvatarStore.load(this);
        if (saved != null) {
            preview.setImageBitmap(saved);
            avatarState.setText("Avatar ready • camera chooser mode enabled");
            testChooser.setEnabled(true);
        }
    }

    private void buildUi() {
        int white = Color.WHITE;
        int muted = Color.rgb(182, 182, 192);
        int bg = Color.rgb(10, 10, 12);
        int panel = Color.rgb(23, 23, 27);
        int accent = Color.rgb(217, 255, 104);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(32));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        TextView title = text("AvatarCam", 34, white);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView subtitle = text("Choose an avatar, then select AvatarCam whenever Android offers a camera-app chooser.", 16, muted);
        LinearLayout.LayoutParams subP = new LinearLayout.LayoutParams(-1, -2);
        subP.setMargins(0, dp(6), 0, dp(20));
        root.addView(subtitle, subP);

        preview = new ImageView(this);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setBackgroundColor(panel);
        LinearLayout.LayoutParams imageP = new LinearLayout.LayoutParams(-1, dp(360));
        root.addView(preview, imageP);

        avatarState = text("No avatar selected", 14, muted);
        avatarState.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams stateP = new LinearLayout.LayoutParams(-1, -2);
        stateP.setMargins(0, dp(10), 0, dp(12));
        root.addView(avatarState, stateP);

        Button choose = button("Add avatar photo");
        choose.setOnClickListener(v -> pickAvatar());
        root.addView(choose, new LinearLayout.LayoutParams(-1, -2));

        testChooser = button("Test camera chooser");
        testChooser.setEnabled(false);
        testChooser.setOnClickListener(v -> testCameraChooser());
        LinearLayout.LayoutParams testP = new LinearLayout.LayoutParams(-1, -2);
        testP.setMargins(0, dp(8), 0, 0);
        root.addView(testChooser, testP);

        TextView enabled = text("✓ Camera chooser mode enabled", 18, accent);
        enabled.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams enabledP = new LinearLayout.LayoutParams(-1, -2);
        enabledP.setMargins(0, dp(22), 0, dp(6));
        root.addView(enabled, enabledP);

        TextView help = text(
                "When another app asks Android to take a photo using a camera app, choose AvatarCam from the chooser. AvatarCam will return your saved avatar as the captured image. Apps that open the physical camera hardware directly do not show this chooser.",
                15, muted);
        help.setPadding(dp(16), dp(16), dp(16), dp(16));
        help.setBackgroundColor(panel);
        root.addView(help, new LinearLayout.LayoutParams(-1, -2));

        Button camera = button("Allow front camera for future animation");
        camera.setOnClickListener(v -> requestAnimationCamera());
        LinearLayout.LayoutParams cameraP = new LinearLayout.LayoutParams(-1, -2);
        cameraP.setMargins(0, dp(16), 0, 0);
        root.addView(camera, cameraP);

        TextView privacy = text("Use real people's likenesses only with permission. AvatarCam is not intended to bypass biometric or identity verification.", 12, muted);
        LinearLayout.LayoutParams privacyP = new LinearLayout.LayoutParams(-1, -2);
        privacyP.setMargins(0, dp(16), 0, 0);
        root.addView(privacy, privacyP);

        setContentView(scroll);
    }

    private void pickAvatar() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }

    private void testCameraChooser() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent chooser = Intent.createChooser(cameraIntent, "Choose camera");
        try {
            startActivityForResult(chooser, TEST_CAMERA);
        } catch (Exception e) {
            Toast.makeText(this, "No compatible camera apps were found.", Toast.LENGTH_LONG).show();
        }
    }

    private void requestAnimationCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Front-camera permission is already enabled.", Toast.LENGTH_SHORT).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;
            try {
                Bitmap b = AvatarStore.importFromUri(this, uri);
                preview.setImageBitmap(b);
                avatarState.setText("Avatar ready • camera chooser mode enabled");
                testChooser.setEnabled(true);
            } catch (Exception e) {
                Toast.makeText(this, "Could not import image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return;
        }

        if (requestCode == TEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.getExtras() != null) {
                    Object result = data.getExtras().get("data");
                    if (result instanceof Bitmap) {
                        preview.setImageBitmap((Bitmap) result);
                    }
                }
                Toast.makeText(this, "Camera chooser returned an image successfully.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Camera test cancelled.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
