package ru.dsci.qrvisor.core;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
public class QRTools {

    private static Result decodeBitmap(BinaryBitmap binaryBitmap) {
        Result result;
        try {
            result = new MultiFormatReader().decode(binaryBitmap);
        } catch (NotFoundException e) {
            throw new IllegalArgumentException(String.format("Image does not contain QR-code: %s", e.getMessage()));
        }
        return result;
    }

    private static BinaryBitmap getBitmapFromUrl(String url) throws IOException {
        BinaryBitmap binaryBitmap;
        try {
            binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                    new BufferedImageLuminanceSource(ImageIO.read(new URL(url)))));
        } catch (IOException e) {
            log.error(String.format("{QRTools.getBitmapFromUrl}: %s", e.getMessage()));
            throw new IOException(String.format("Unable to decrypt QR-code: %s", e.getMessage()));
        }
        return binaryBitmap;
    }

    public static String getCertUrlFromQR(String url) throws IOException {
        String text;
        try {
            Result result = decodeBitmap(getBitmapFromUrl(url));
            if (result.getBarcodeFormat() != BarcodeFormat.QR_CODE)
                throw new IOException(String.format(
                        "Incorrect QR-code format '%s', expected: %s"
                        , result.getBarcodeFormat(), BarcodeFormat.QR_CODE));
            text = result.getText();
        } catch (RuntimeException | MalformedURLException e) {
            log.debug(String.format("{decodeQR}: %s", e.getMessage()));
            throw new IOException(String.format("Unable to decrypt QR-code: %s", e.getMessage()));
        }
        return text;
    }

}
