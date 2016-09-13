package be.ehb.watchin.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import be.ehb.watchin.R;
import be.ehb.watchin.model.Event;

public class QRGenActivity extends AppCompatActivity {

    private static final int WIDTH = 1000;
    public static final String QR_DATA = "be.ehb.watchin.QR_DATA";
    public static final String EVENT = "be.ehb.watchin.EVENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrgen);

        //String qr_data = getIntent().getStringExtra(QR_DATA);
        Event event = (Event) getIntent().getSerializableExtra(EVENT);

        ImageView ivQRcode = (ImageView) findViewById(R.id.ivQRcode);
        TextView txtEventName = (TextView) findViewById(R.id.txtEventName);

        txtEventName.setText(event.getName());
        // this is a small sample use of the QRCodeEncoder class from zxing
        try {
            Bitmap bm = encodeAsBitmap(event.getUuid().toString());

            if(bm != null) {
                ivQRcode.setImageBitmap(bm);
            }
        } catch (WriterException e) { }
    }

    private Bitmap encodeAsBitmap(String str) throws WriterException {


        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.AZTEC, WIDTH , WIDTH, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, WIDTH, 0, 0, w, h);
        return bitmap;
    } /// end of this method

}
