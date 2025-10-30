package com.example.carbon;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QRCodeGenerator {

    // Define your app's custom scheme/prefix for the deep link
    private static final String APP_SCHEME = "carbondate://events/";
    private static final int QR_SIZE = 500; // 500x500 pixels

    /**
     * Generates a Bitmap image of a QR code that encodes the event's deep link.
     * @param eventUuid The unique UUID of the event.
     * @return A Bitmap representing the QR code, or null on failure.
     */
    public static Bitmap generateQRCode(UUID eventUuid) {
        // 1. Construct the complete deep link URL
        String content = APP_SCHEME + eventUuid.toString();

        // 2. Setup Zxing hints
        Map<EncodeHintType, Object> hints = new HashMap<>();
        // Set error correction level to HIGH for better scannability
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        try {
            // 3. Encode the content into a BitMatrix
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    QR_SIZE,
                    QR_SIZE,
                    hints
            );

            // 4. Convert the BitMatrix to a Bitmap
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // Set black for data points, white for background
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;

        } catch (WriterException e) {
            e.printStackTrace();
            return null; // Handle this error in your UI
        }
    }
}
