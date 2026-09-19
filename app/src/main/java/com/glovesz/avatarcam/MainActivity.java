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
import android.provider.Settings;
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

    private ImageView preview;
    private TextView avatarState;
    private TextView compatibility;
    private Button activate;

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
            avatarState.setText("Avatar ready • stored on this phone");
            activate.setEnabled(true);
        }
        refreshCompatibility();
    }

    private void buildUi() {
        int white = Color.WHITE;
        int muted = Color.rgb(182, 182, 192);
        int bg = Color.rgb(10, 10, 12);
        int panel = Color.rgb(23, 23, 27);

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

        TextView subtitle = text("Use a chosen avatar as your camera image where Android allows it.", 16, muted);
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

        Button camera = button("Allow front camera for animation");
        camera.setOnClickListener(v -> requestAnimationCamera());
        LinearLayout.LayoutParams cameraP = new LinearLayout.LayoutParams(-1, -2);
        cameraP.setMargins(0, dp(8), 0, 0);
        root.addView(camera, cameraP);

        TextView compTitle = text("Camera compatibility", 20, white);
        compTitle.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams compTitleP = new LinearLayout.LayoutParams(-1, -2);
        compTitleP.setMargins(0, dp(24), 0, dp(8));
        root.addView(compTitle, compTitleP);

        compatibility = text("Checking…", 15, muted);
        compatibility.setPadding(dp(16), dp(16), dp(16), dp(16));
        compatibility.setBackgroundColor(panel);
        root.addView(compatibility, new LinearLayout.LayoutParams(-1, -2));

        activate = button("Activate AvatarCam");
        activate.setEnabled(false);
        activate.setOnClickListener(v -> activate());
        LinearLayout.LayoutParams actP = new LinearLayout.LayoutParams(-1, -2);
        actP.setMargins(0, dp(16), 0, 0);
        root.addView(activate, actP);

        TextView privacy = text("Use real people's likenesses only with permission. AvatarCam does not attempt to bypass biometric or identity verification.", 12, muted);
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
        if (requestCode != PICK_IMAGE || resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;
        try {
            Bitmap b = AvatarStore.importFromUri(this, uri);
            preview.setImageBitmap(b);
            avatarState.setText("Avatar ready • stored on this phone");
            activate.setEnabled(true);
        } catch (Exception e) {
            Toast.makeText(this, "Could not import image: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void refreshCompatibility() {
        VirtualCameraProbe.Result r = VirtualCameraProbe.run(this);
        String api;
        switch (r.state) {
            case SUPPORTED: api = "Virtual-camera framework: SUPPORTED ✓"; break;
            case UNSUPPORTED: api = "Virtual-camera framework: not supported by this firmware"; break;
            case API_NOT_PRESENT: api = "Virtual-camera API extension: not present"; break;
            default: api = "Virtual-camera check: error"; break;
        }
        String permission = r.hasPrivilegedPermission
                ? "Virtual-device role permission: GRANTED ✓"
                : "Virtual-device role permission: not granted";
        String config = r.configClassPresent
                ? "VirtualCameraConfig class: present"
                : "VirtualCameraConfig class: unavailable";
        compatibility.setText(api + "\n" + permission + "\n" + config + "\n\n" + r.detail);
    }

    private void activate() {
        VirtualCameraProbe.Result r = VirtualCameraProbe.run(this);
        if (r.state == VirtualCameraProbe.State.SUPPORTED && r.hasPrivilegedPermission) {
            Toast.makeText(this, "Virtual-camera host is available. The next build can attach the avatar renderer.", Toast.LENGTH_LONG).show();
            return;
        }
        if (r.state == VirtualCameraProbe.State.SUPPORTED) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Framework supported — permission blocked")
                    .setMessage("This phone reports virtual-camera support, but Android has not granted AvatarCam the privileged virtual-device role. A normal APK cannot silently grant itself that role. The next route is a supported system role or a device-level/root helper.")
                    .setPositiveButton("OK", null)
                    .setNeutralButton("App settings", (d, w) -> {
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                        startActivity(i);
                    })
                    .show();
        } else {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("System-wide mode unavailable")
                    .setMessage("Your current Android build does not expose a usable virtual-camera path to this normal APK. The selected avatar is working locally; replacing the front camera in other apps requires a device-level helper on this firmware.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}
