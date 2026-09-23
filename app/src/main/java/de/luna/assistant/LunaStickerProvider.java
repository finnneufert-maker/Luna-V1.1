package de.luna.assistant;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

/** Exposes one selected atlas cell to Android's secure share sheet. */
public final class LunaStickerProvider extends ContentProvider {
    @Override public boolean onCreate() { return true; }
    @Override public String getType(Uri uri) { return "image/png"; }

    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        int frame;
        try { frame=Math.max(0,Math.min(7,Integer.parseInt(uri.getLastPathSegment()))); }
        catch(Exception e) { frame=0; }
        int[] ids={R.drawable.luna_emoji_wave,R.drawable.luna_emoji_thumbsup,
                R.drawable.luna_emoji_thinking,R.drawable.luna_emoji_surprised,
                R.drawable.luna_emoji_sorry,R.drawable.luna_emoji_sleep,
                R.drawable.luna_emoji_excited,R.drawable.luna_emoji_ok};
        Bitmap sticker=BitmapFactory.decodeResource(getContext().getResources(),ids[frame]);
        File out=new File(getContext().getCacheDir(),"luna_chibi_"+frame+".png");
        try(FileOutputStream stream=new FileOutputStream(out)) { sticker.compress(Bitmap.CompressFormat.PNG,100,stream); }
        catch(Exception e) { throw new FileNotFoundException("Sticker konnte nicht erstellt werden."); }
        return ParcelFileDescriptor.open(out,ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public Cursor query(Uri u,String[] p,String s,String[] a,String sort){return null;}
    @Override public Uri insert(Uri u,ContentValues v){return null;}
    @Override public int delete(Uri u,String s,String[] a){return 0;}
    @Override public int update(Uri u,ContentValues v,String s,String[] a){return 0;}
}
