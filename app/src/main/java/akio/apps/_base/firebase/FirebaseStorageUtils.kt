package akio.apps._base.firebase

import akio.apps._base.ui.BitmapUtils
import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*

object FirebaseStorageUtils {

    suspend fun uploadBitmap(storageRef: StorageReference, bitmap: Bitmap, scaledSize: Int): Uri {
        val storeName = UUID.randomUUID().toString()
        val photoRef = storageRef.child(storeName)

        val uploadBitmap = BitmapUtils.scale(bitmap, scaledSize)
        val uploadBaos = ByteArrayOutputStream()
        uploadBitmap.compress(Bitmap.CompressFormat.JPEG, 100, uploadBaos)
        val uploadData = uploadBaos.toByteArray()
        photoRef.putBytes(uploadData).await()
        uploadBaos.close()
        uploadBitmap.recycle()
        return photoRef.downloadUrl.await()
    }

}