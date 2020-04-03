package com.example.android.arrival.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.android.arrival.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/* Citation:
    Name: Juned Mughal
    Title: Generate QR code in android using Zxing library in Android Studio example tutorial
    Link: https://www.android-examples.com/generate-qr-code-in-android-using-zxing-library-in-android-studio/
 */

/**
 * DialogFragment displayed when generating a QR for payment.
 * Generates and displays a QR to be scanned by a Driver.
 */
public class DisplayQRDialog extends DialogFragment {


    private Bitmap bitmap;
    private ImageView imageView;
    private String textToConvert;
    private final static int QRCodeWidth = 800 ;


    public static DisplayQRDialog newInstance(String s) {
        Bundle args = new Bundle();
        args.putSerializable("convert", s);
        DisplayQRDialog fragment = new DisplayQRDialog();
        fragment.setArguments(args);
        return fragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_generate_qr, null);
        assert getArguments() != null;
        textToConvert = (String) getArguments().getSerializable("convert");
        imageView = view.findViewById(R.id.barcode);

        try {
            bitmap = TextToImageEncode(textToConvert);
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Payment")
                .setNegativeButton("CLOSE", null).create();

    }

    /**
     * This encodes the input string into a QR code bitmap
     * @param Value String
     * @return Bitmap
     * @throws WriterException
     */

    private Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.QR_CODE,
                    QRCodeWidth, QRCodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.black):getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 800, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

}
