package de.hadizadeh.positioning.roommodel.android.technologies;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.*;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;

/**
 * Implementation for the nfc system
 */
public class NfcHandler implements NfcHandlerInterface {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techList;
    private Context context;
    private String utf8Charset;
    private String packageName;

    /**
     * Creates the nfc management
     *
     * @param context context
     */
    public NfcHandler(Context context) {
        this.context = context;
        this.packageName = this.context.getPackageName();
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        utf8Charset = "UTF-8";
    }

    /*
     * (non-Javadoc)
     *
     * @see de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#startScanning()
     */
    @Override
    public void startScanning() {
        pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                PendingIntent.FLAG_UPDATE_CURRENT), 0);

        IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        tag.addCategory(Intent.CATEGORY_DEFAULT);
        try {
            tag.addDataType("*/*");
            ndef.addDataType("*/*");
            tech.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
        }
        intentFiltersArray = new IntentFilter[]{tag, ndef, tech};

        techList = new String[][]{new String[]{NfcA.class.getName(), MifareUltralight.class.getName(), Ndef.class.getName()}, new String[]{NfcA
                .class.getName(),}, new String[]{Ndef.class.getName()}, new String[]{MifareUltralight.class.getName()}};
    }

    /*
     * (non-Javadoc)
     *
     * @see de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#resume()
     */
    @Override
    public void resume() {
        try {
            nfcAdapter.enableForegroundDispatch((Activity) context, pendingIntent, intentFiltersArray, techList);
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "could not enable NFC foreground dispatcher.");
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#pause()
     */
    @Override
    public void pause() {
        try {
            nfcAdapter.disableForegroundDispatch((Activity) context);
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "could not disable NFC foreground dispatcher.");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#tagMatches(java.lang
     * .String)
     */
    @Override
    public boolean tagMatches(String action) {
        return (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter
                .ACTION_NDEF_DISCOVERED.equals(action));
    }

    /* (non-Javadoc)
     * @see de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#getTagId(android.content.Intent)
     */
    @Override
    public String getTagId(Intent intent) {
        Tag nfcTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] tagId = nfcTag.getId();
        StringBuilder stringBuilder = new StringBuilder("0x");
        char[] buffer = new char[2];
        for (int i = 0; i < tagId.length; i++) {
            buffer[0] = Character.forDigit((tagId[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(tagId[i] & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#read(android.content
     * .Intent)
     */
    @Override
    public String read(Intent intent) {
        byte[] data = readBytes(intent);
        if (data != null) {
            return new String(data);
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#readBytes(android.content
     * .Intent)
     */
    @Override
    public byte[] readBytes(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs != null) {
            NdefMessage msg = (NdefMessage) rawMsgs[0];
            return msg.getRecords()[0].getPayload();
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#write(java.lang.String,
     * android.nfc.Tag)
     */
    @Override
    public void write(String text, Tag tag) throws IOException, FormatException {
        write(text.getBytes(utf8Charset), tag);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#write(byte[],
     * android.nfc.Tag)
     */
    @Override
    public void write(byte[] data, Tag tag) throws IOException, FormatException {
        write(data, tag, NdefRecord.TNF_ABSOLUTE_URI, NdefRecord.RTD_URI);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#write(byte[],
     * android.nfc.Tag, short, byte[])
     */
    @Override
    public void write(byte[] data, Tag tag, short tnf, byte[] mime) throws IOException, FormatException {
        NdefMessage message = new NdefMessage(new NdefRecord[]{new NdefRecord(tnf, mime, new byte[0], data)});

        write(message, tag);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#writeWithFallback(byte
     * [], android.nfc.Tag, short, byte[])
     */
    @Override
    public void writeWithFallback(byte[] data, Tag tag, short tnf, byte[] mime) throws IOException, FormatException {
        NdefMessage message = new NdefMessage(new NdefRecord[]{new NdefRecord(tnf, mime, new byte[0], data), NdefRecord.createApplicationRecord(this.packageName)});

        write(message, tag);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#write(android.nfc.
     * NdefMessage, android.nfc.Tag)
     */
    @Override
    public void write(NdefMessage message, Tag tag) throws IOException, FormatException {
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            ndef.connect();
            ndef.writeNdefMessage(message);
            ndef.close();
        } else {
            NdefFormatable format = NdefFormatable.get(tag);
            format.connect();
            format.format(message);
            format.close();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#getTagSize(android.
     * nfc.Tag)
     */
    @Override
    public int getTagSize(Tag tag) {
        Ndef ndefTag = Ndef.get(tag);
        if (ndefTag != null) {
            return ndefTag.getMaxSize();
        } else {
            return -1;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#isTagWritable(android
     * .nfc.Tag)
     */
    @Override
    public boolean isTagWritable(Tag tag) {
        Ndef ndefTag = Ndef.get(tag);
        if (ndefTag != null) {
            return ndefTag.isWritable();
        } else {
            return true;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#isNfcEnabled()
     */
    @Override
    public boolean isNfcEnabled() {
        return nfcAdapter.isEnabled();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#enableNfc(android.content
     * .DialogInterface.OnClickListener)
     */
    @Override
    public void enableNfc(DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setMessage(context.getString(R.string.activity_nfchandler_enable_nfc)).setPositiveButton(context.getString(R.string.activity_nfchandler_enable_nfc_yes), listener).setNegativeButton(context.getString(R.string.activity_nfchandler_enable_nfc_no), listener).setCancelable(false).show();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.hadizadeh.positioning.roommodel.android.technologies.NfcHandlerInterface#startNfcSettings()
     */
    @Override
    public void startNfcSettings() {
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            ((Activity) context).startActivityForResult(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS), RESULT_NFC_ENABLED);
        } else {
            ((Activity) context).startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), RESULT_NFC_ENABLED);
        }
    }

}
