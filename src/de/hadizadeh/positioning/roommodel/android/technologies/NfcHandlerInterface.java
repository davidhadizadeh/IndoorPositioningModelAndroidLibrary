package de.hadizadeh.positioning.roommodel.android.technologies;

import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.Tag;

import java.io.IOException;

/**
 * Interface for handling nfc data
 */
public interface NfcHandlerInterface {

    /**
     * Defines that nfc is enabled
     */
    public static int RESULT_NFC_ENABLED = 1;

    /**
     * Starts scanning for nfc tags
     */
    public void startScanning();

    /**
     * Enables scanning in the forground
     */
    public void resume();

    /**
     * Pauses scanning
     */
    public void pause();

    /**
     * Defines if and nfc tag is compatible
     *
     * @param action action of the tag
     * @return true, if the action is supported, else it is not
     */
    public boolean tagMatches(String action);

    /**
     * Returns the id of the nfc tag as hex string
     *
     * @param intent intent of the tag
     * @return id of the tag
     */
    public String getTagId(Intent intent);


    /**
     * Reads the nfc data as text
     *
     * @param intent intent of the tag
     * @return text of the tag
     */
    public String read(Intent intent);

    /**
     * Reads the raw data of a tag into an byte array
     *
     * @param intent intent of the tag
     * @return raw data
     */
    public byte[] readBytes(Intent intent);

    /**
     * Writes a text to a tag
     *
     * @param text text to be written
     * @param tag  nfc tag
     * @throws IOException     if writing permission fails
     * @throws FormatException if data are not in the correct format
     */
    public void write(String text, Tag tag) throws IOException, FormatException;

    /**
     * Writes data to a tag
     *
     * @param data data
     * @param tag  nfc tag
     * @throws IOException     if writing permission fails
     * @throws FormatException if data are not in the correct format
     */
    public void write(byte[] data, Tag tag) throws IOException, FormatException;

    /**
     * Writes data with defined message and mime type to a nfc tag
     *
     * @param data data
     * @param tag  nfc tag
     * @param tnf  type of the data
     * @param mime mime type to connect the correct application with the tag
     * @throws IOException     if writing permission fails
     * @throws FormatException if data are not in the correct format
     */
    public void write(byte[] data, Tag tag, short tnf, byte[] mime) throws IOException, FormatException;

    /**
     * Writes data to a nfc tag and also writes a fallback package name to it. If there is no app to open the tag data, then this package will be looked up in the store
     *
     * @param data data
     * @param tag  nfctag
     * @param tnf  type of the data
     * @param mime mime type to connect the correct application with the tag
     * @throws IOException     if writing permission fails
     * @throws FormatException if data are not in the correct format
     */
    public void writeWithFallback(byte[] data, Tag tag, short tnf, byte[] mime) throws IOException, FormatException;

    /**
     * Writes a message to a nfc tag
     *
     * @param message message
     * @param tag     nfc tag
     * @throws IOException     if writing permission fails
     * @throws FormatException if data are not in the correct format
     */
    public void write(NdefMessage message, Tag tag) throws IOException, FormatException;

    /**
     * Returns the maximum tag size
     *
     * @param tag nfc tag
     * @return maximum tag size
     */
    public int getTagSize(Tag tag);

    /**
     * Returns if a nfc tag is writable
     *
     * @param tag nfc tag
     * @return true, if the tag is writable, else it is not writable
     */
    public boolean isTagWritable(Tag tag);

    /**
     * Returns if nfc is enabled on the system
     *
     * @return true, if it is enabled, else it is not
     */
    public boolean isNfcEnabled();

    /**
     * Asks for enabling nfc
     *
     * @param listener listener for requesting the answer
     */
    public void enableNfc(DialogInterface.OnClickListener listener);

    /**
     * Opens the nfc settings
     */
    public void startNfcSettings();

}