package it.letscode.simappdevice;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import org.json.JSONObject;

import java.io.File;

public class MemoryInfoHelper {
    
    // Metoda zwracająca ilość pamięci RAM używanej przez aplikację w MB
    public static long getUsedMemorySize() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    // Metoda zwracająca całkowitą ilość dostępnej pamięci RAM w urządzeniu w MB
    public static long getTotalMemorySize() {
        ActivityManager activityManager = (ActivityManager) ApplicationContextProvider.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.totalMem;
    }

    // Metoda zwracająca ilość ogólnie zużytej pamięci RAM w urządzeniu w MB
    public static long getUsedMemorySizeTotal() {
        ActivityManager activityManager = (ActivityManager) ApplicationContextProvider.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long totalMemoryInBytes = memoryInfo.totalMem;
        long availableMemoryInBytes = memoryInfo.availMem;
        return totalMemoryInBytes - availableMemoryInBytes;
    }


    ////////////////////////////////////

    // Metoda zwracająca całkowitą przestrzeń dyskową w MB
    public static long getTotalDiskSpace() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return ((long) statFs.getBlockCount()) * statFs.getBlockSize();
    }

    // Metoda zwracająca dostępną przestrzeń dyskową w MB
    public static long getFreeDiskSpace() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return ((long) statFs.getAvailableBlocks()) * statFs.getBlockSize();
    }

    // Metoda zwracająca zajętą przestrzeń dyskową przez aplikację w MB
    public static long getAppUsedSpace() {
        long totalSize = 0;
        File appPath = ApplicationContextProvider.getApplicationContext().getFilesDir();
        totalSize += getFolderSize(appPath);

        // Dodaj przestrzeń używaną przez cache aplikacji
        File cachePath = ApplicationContextProvider.getApplicationContext().getCacheDir();
        totalSize += getFolderSize(cachePath);

        return totalSize;
    }

    // Pomocnicza metoda do obliczania rozmiaru folderu
    private static long getFolderSize(File file) {
        long size = 0;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                size += getFolderSize(child);
            }
        } else {
            size = file.length();
        }
        return size;
    }


    public static long getUsedStorageTotal() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());

        long totalBlocks = statFs.getBlockCountLong();
        long availableBlocks = statFs.getAvailableBlocksLong();

        long blockSize = statFs.getBlockSizeLong();

        return (totalBlocks - availableBlocks) * blockSize;

    }


    public static JSONObject getMemoryInfoAsJson() {
        try {

            // to MB: X / (1024 * 1024)

            JSONObject memoryInfoJson = new JSONObject();
            memoryInfoJson.put("usedMemoryByApp", getUsedMemorySize());
            memoryInfoJson.put("totalMemory", getTotalMemorySize());
            memoryInfoJson.put("usedMemoryTotal", getUsedMemorySizeTotal());

            memoryInfoJson.put("totalDiskSpace", getTotalDiskSpace());
            memoryInfoJson.put("freeDiskSpace", getFreeDiskSpace());
            memoryInfoJson.put("appUsedSpace", getAppUsedSpace());
            memoryInfoJson.put("usedStorageTotal", getUsedStorageTotal());

            return memoryInfoJson;
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
