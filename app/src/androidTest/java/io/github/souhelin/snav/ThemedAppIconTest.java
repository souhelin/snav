package io.github.souhelin.snav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ThemedAppIconTest {
    @Test public void adaptiveIconKeepsColorLayers() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Drawable icon = context.getDrawable(context.getApplicationInfo().icon);
        assertTrue(icon instanceof AdaptiveIconDrawable);
        AdaptiveIconDrawable adaptive = (AdaptiveIconDrawable) icon;
        assertNotNull(adaptive.getBackground());
        assertNotNull(adaptive.getForeground());
    }

    @Test public void android13IconHasVisibleMonochromeSilhouette() {
        assumeTrue(Build.VERSION.SDK_INT >= 33);
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AdaptiveIconDrawable icon = (AdaptiveIconDrawable) context.getDrawable(context.getApplicationInfo().icon);
        Drawable monochrome = icon.getMonochrome();
        assertNotNull("A camada monocromática deve estar presente no Android 13+", monochrome);
        Bitmap bitmap = Bitmap.createBitmap(432, 432, Bitmap.Config.ARGB_8888);
        monochrome.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        monochrome.draw(new Canvas(bitmap));
        assertEquals("O fundo da máscara deve ser transparente", 0, bitmap.getPixel(0, 0) >>> 24);
        int visible = 0;
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                if ((bitmap.getPixel(x, y) >>> 24) > 32) visible++;
            }
        }
        assertTrue("A máscara deve conter a silhueta do S", visible > 1000);
        assertTrue("A máscara não deve preencher todo o ícone", visible < bitmap.getWidth() * bitmap.getHeight() / 2);
        bitmap.recycle();
    }
}
