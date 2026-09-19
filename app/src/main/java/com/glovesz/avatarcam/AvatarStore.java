package com.glovesz.avatarcam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

final class AvatarStore {
    private static final String FILE_NAME = "avatar.jpg";

    private AvatarStore() {}

    static File file(Context context) {
        return new File(context.getFilesDir(), FILE_NAME);
    }

    static Bitmap load(Context context) {
        File f = file(context);
        if (!f.exists()) return null;
        return BitmapFactory.decodeFile(f.getAbsolutePath());
    }

    static Bitmap importFromUri(Context context, Uri uri) throws Exception {
        Bitmap source;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            source = BitmapFactory.decodeStream(in);
        }
        if (source == null) throw new IllegalArgumentException("That image could not be opened.");

        int max = 1800;
        int w = source.getWidth();
        int h = source.getHeight();
        Bitmap out = source;
        if (Math.max(w, h) > max) {
            float scale = (float) max / (float) Math.max(w, h);
            out = Bitmap.createScaledBitmap(source, Math.max(1, Math.round(w * scale)), Math.max(1, Math.round(h * scale)), true);
            if (out != source) source.recycle();
        }

        try (FileOutputStream fos = new FileOutputStream(file(context))) {
            out.compress(Bitmap.CompressFormat.JPEG, 94, fos);
        }
        return out;
    }
}
