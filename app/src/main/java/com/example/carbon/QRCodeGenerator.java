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

/**
 * The QRCodeGenerator generates a QR code... Duh.
 * It has methods to create a bitmap image, but the object is not stored in the DB,
 * as storing the QR code's image is more costly than just creating it again when needed
 *
 * @author Cooper Goddard
 */
public class QRCodeGenerator {

    // Define custom link within app
    private static final String APP_SCHEME = "carbondate://events/"; //TODO fix this with proper link
    private static final int QR_SIZE = 500; // 500x500 pixels

    /**
     * Generates a Bitmap image of a QR code that encodes the event's deep link.
     * @param eventUuid The unique UUID of the event.
     * @return A Bitmap representing the QR code, or null on failure.
     *
     * @author Cooper Goddard
     */
    public static Bitmap generateQRCode(UUID eventUuid) {
        String content = APP_SCHEME + eventUuid.toString();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        // Set error correction level to HIGH for better scannability
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        try {
            // Encode the content into a BitMatrix
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    QR_SIZE,
                    QR_SIZE,
                    hints
            );

            // Convert the BitMatrix to a Bitmap
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
            return null;
        }
    }
}
