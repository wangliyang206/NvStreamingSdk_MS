package com.meishe.base.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.documentfile.provider.DocumentFile;

import com.meishe.app.BaseApp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2016/05/03
 *     desc  : utils about file
 * </pre>
 * 文件工具类
 * File tool class
 */
public final class FileUtils {
    private static final int BUFFER_SIZE = 8192;
    private static final char[] HEX_DIGITS_UPPER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final char[] HEX_DIGITS_LOWER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String LINE_SEP = System.getProperty("line.separator");

    private FileUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static final int BYTE = 1;
    public static final int KB   = 1024;
    public static final int MB   = 1048576;
    public static final int GB   = 1073741824;
    /**
     * Return the file by path.
     * 按路径返回文件
     * @param filePath The path of file. 文件的路径
     * @return the file
     */
    public static File getFileByPath(final String filePath) {
        return StringUtils.isSpace(filePath) ? null : new File(filePath);
    }

    /**
     * Return whether the file exists.
     *  返回文件是否存在
     * @param file The file.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isFileExists(final File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return true;
        }
        return isFileExists(file.getAbsolutePath());
    }

    /**
     * Return whether the file exists.
     * 返回文件是否存在
     * @param filePath The path of file. 文件路径
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isFileExists(final String filePath) {
        File file = getFileByPath(filePath);
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return true;
        }
        return isFileExistsApi29(filePath);
    }

    private static boolean isFileExistsApi29(String filePath) {
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                Uri uri = Uri.parse(filePath);
                DocumentFile documentFile =
                        DocumentFile.fromSingleUri(BaseApp.getContext(), uri);
                return documentFile.exists();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * Rename the file.
     * 重命名文件
     * @param filePath The path of file. 文件路径
     * @param newName  The new name of file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean rename(final String filePath, final String newName) {
        return rename(getFileByPath(filePath), newName);
    }


    public static boolean rename(final File file, final String newName) {
        /*
        * file is null then return false
        * 文件为空，然后返回false
        * */
        if (file == null) {
            return false;
        }
        /*
        * file doesn't exist then return false
        * 文件不存在，然后返回false
        * */
        if (!file.exists()) {
            return false;
        }

        /*
        * the new name is space then return false
        * 新名称为space，然后返回false
        * */
        if (StringUtils.isSpace(newName)) {
            return false;
        }
        /*
        * the new name equals old name then return true
        * 新名称等于旧名称，然后返回true
        * */
        if (newName.equals(file.getName())) {
            return true;
        }
        File newFile = new File(file.getParent() + File.separator + newName);
        /*
        * the new name of file exists then return false
        * 如果文件的新名称存在，则返回false
        * */
        return !newFile.exists()
                && file.renameTo(newFile);
    }

    /**
     * Return whether it is a directory.
     * 返回它是否是一个目录
     * @param dirPath The path of directory. 目录的路径
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isDir(final String dirPath) {
        return isDir(getFileByPath(dirPath));
    }

    /**
     * Return whether it is a directory.
     * 返回它是否是一个目录
     * @param file The file. 该文件
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isDir(final File file) {
        return file != null && file.exists() && file.isDirectory();
    }

    /**
     * Return whether it is a file.
     * 返回它是否是一个文件
     * @param filePath The path of file. 文件路径
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isFile(final String filePath) {
        return isFile(getFileByPath(filePath));
    }

    /**
     * Return whether it is a file.
     * 返回它是否是一个文件
     * @param file The file.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isFile(final File file) {
        return file != null && file.exists() && file.isFile();
    }

    /**
     * Create a directory if it doesn't exist, otherwise do nothing.
     * 如果目录不存在，就创建它，否则什么都不做
     * @param dirPath The path of directory. 目录的路径
     * @return {@code true}: exists or creates successfully<br>{@code false}: otherwise
     */
    public static boolean createOrExistsDir(final String dirPath) {
        return createOrExistsDir(getFileByPath(dirPath));
    }

    /**
     * Create a directory if it doesn't exist, otherwise do nothing.
     * 如果目录不存在，就创建它，否则什么都不做
     * @param file The file. 该文件
     * @return {@code true}: exists or creates successfully<br>{@code false}: otherwise
     */
    public static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * Create a file if it doesn't exist, otherwise do nothing.
     * 创建一个不存在的文件，否则什么都不做
     * @param filePath The path of file. 文件路径
     * @return {@code true}: exists or creates successfully<br>{@code false}: otherwise
     */
    public static boolean createOrExistsFile(final String filePath) {
        return createOrExistsFile(getFileByPath(filePath));
    }

    /**
     * Create a file if it doesn't exist, otherwise do nothing.
     * 创建一个不存在的文件，否则什么都不做
     * @param file The file. 该文件
     * @return {@code true}: exists or creates successfully<br>{@code false}: otherwise
     */
    public static boolean createOrExistsFile(final File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return file.isFile();
        }
        if (!createOrExistsDir(file.getParentFile())) {
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create a file if it doesn't exist, otherwise delete old file before creating.
     *
     * @param filePath The path of file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean createFileByDeleteOldFile(final String filePath) {
        return createFileByDeleteOldFile(getFileByPath(filePath));
    }

    /**
     * Create a file if it doesn't exist, otherwise delete old file before creating.
     *
     * @param file The file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean createFileByDeleteOldFile(final File file) {
        if (file == null) {
            return false;
        }
        // file exists and unsuccessfully delete then return false
        if (file.exists() && !file.delete()) {
            return false;
        }
        if (!createOrExistsDir(file.getParentFile())) {
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copy the directory or file.
     * 复制目录或文件
     * @param srcPath  The path of source. 源的路径
     * @param destPath The path of destination. 目的地路径
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean copy(final String srcPath,
                               final String destPath) {
        return copy(getFileByPath(srcPath), getFileByPath(destPath), null);
    }

    /**
     * Copy the directory or file.
     * 复制目录或文件
     * @param srcPath  The path of source. 源的路径
     * @param destPath The path of destination. 目的地路径
     * @param listener The replace listener.  取代监听器
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean copy(final String srcPath,
                               final String destPath,
                               final OnReplaceListener listener) {
        return copy(getFileByPath(srcPath), getFileByPath(destPath), listener);
    }

    /**
     * Copy the directory or file.
     * 复制目录或文件
     * @param src  The source. 来源
     * @param dest The destination. 目标
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean copy(final File src,
                               final File dest) {
        return copy(src, dest, null);
    }

    /**
     * Copy the directory or file.
     * 复制目录或文件
     * @param src      The source. 源
     * @param dest     The destination.  目标
     * @param listener The replace listener. 取代监听器
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean copy(final File src,
                               final File dest,
                               final OnReplaceListener listener) {
        if (src == null) {
            return false;
        }
        if (src.isDirectory()) {
            return copyDir(src, dest, listener);
        }
        return copyFile(src, dest, listener);
    }

    /**
     * Copy the directory.
     *
     * @param srcDir   The source directory.
     * @param destDir  The destination directory.
     * @param listener The replace listener.
     * @return {@code true}: success<br>{@code false}: fail
     */
    private static boolean copyDir(final File srcDir,
                                   final File destDir,
                                   final OnReplaceListener listener) {
        return copyOrMoveDir(srcDir, destDir, listener, false);
    }

    /**
     * Copy the file.
     *
     * @param srcFile  The source file.
     * @param destFile The destination file.
     * @param listener The replace listener.
     * @return {@code true}: success<br>{@code false}: fail
     */
    private static boolean copyFile(final File srcFile,
                                    final File destFile,
                                    final OnReplaceListener listener) {
        return copyOrMoveFile(srcFile, destFile, listener, false);
    }

    /**
     * Move the directory or file.
     * 移动目录或文件
     * @param srcPath  The path of source. 源的路径
     * @param destPath The path of destination. 目的地路径
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean move(final String srcPath,
                               final String destPath) {
        return move(getFileByPath(srcPath), getFileByPath(destPath), null);
    }

    /**
     * Move the directory or file.
     *
     * @param srcPath  The path of source.
     * @param destPath The path of destination.
     * @param listener The replace listener.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean move(final String srcPath,
                               final String destPath,
                               final OnReplaceListener listener) {
        return move(getFileByPath(srcPath), getFileByPath(destPath), listener);
    }

    /**
     * Move the directory or file.
     *
     * @param src  The source.
     * @param dest The destination.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean move(final File src,
                               final File dest) {
        return move(src, dest, null);
    }

    /**
     * Move the directory or file.
     *
     * @param src      The source.
     * @param dest     The destination.
     * @param listener The replace listener.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean move(final File src,
                               final File dest,
                               final OnReplaceListener listener) {
        if (src == null) {
            return false;
        }
        if (src.isDirectory()) {
            return moveDir(src, dest, listener);
        }
        return moveFile(src, dest, listener);
    }

    /**
     * Move the directory.
     * 移动目录
     * @param srcDir   The source directory. 源目录
     * @param destDir  The destination directory. 目标目录
     * @param listener The replace listener. 取代监听器
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean moveDir(final File srcDir,
                                  final File destDir,
                                  final OnReplaceListener listener) {
        return copyOrMoveDir(srcDir, destDir, listener, true);
    }

    /**
     * Move the file.
     *
     * @param srcFile  The source file.
     * @param destFile The destination file.
     * @param listener The replace listener.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean moveFile(final File srcFile,
                                   final File destFile,
                                   final OnReplaceListener listener) {
        return copyOrMoveFile(srcFile, destFile, listener, true);
    }

    private static boolean copyOrMoveDir(final File srcDir,
                                         final File destDir,
                                         final OnReplaceListener listener,
                                         final boolean isMove) {
        if (srcDir == null || destDir == null) {
            return false;
        }
        // destDir's path locate in srcDir's path then return false
        String srcPath = srcDir.getPath() + File.separator;
        String destPath = destDir.getPath() + File.separator;
        if (destPath.contains(srcPath)) {
            return false;
        }
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            return false;
        }
        if (!createOrExistsDir(destDir)) {
            return false;
        }
        File[] files = srcDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                File oneDestFile = new File(destPath + file.getName());
                if (file.isFile()) {
                    if (!copyOrMoveFile(file, oneDestFile, listener, isMove)) {
                        return false;
                    }
                } else if (file.isDirectory()) {
                    if (!copyOrMoveDir(file, oneDestFile, listener, isMove)) {
                        return false;
                    }
                }
            }
        }
        return !isMove || deleteDir(srcDir);
    }

    private static boolean copyOrMoveFile(final File srcFile,
                                          final File destFile,
                                          final OnReplaceListener listener,
                                          final boolean isMove) {
        if (srcFile == null || destFile == null) {
            return false;
        }
        /*
        * srcFile equals destFile then return false
        * srcFile = destFile然后返回false
        * */
        if (srcFile.equals(destFile)) {
            return false;
        }
        /*
        * srcFile doesn't exist or isn't a file then return false
        * srcFile不存在或者不是文件，然后返回false
        * */
        if (!srcFile.exists() || !srcFile.isFile()) {
            return false;
        }
        if (destFile.exists()) {
            if (listener == null || listener.onReplace(srcFile, destFile)) {// require delete the old file
                if (!destFile.delete()) {// unsuccessfully delete then return false
                    return false;
                }
            } else {
                return true;
            }
        }
        if (!createOrExistsDir(destFile.getParentFile())) {
            return false;
        }
        try {
            return FileIOUtils.writeFileFromIS(destFile.getAbsolutePath(), new FileInputStream(srcFile))
                    && !(isMove && !deleteFile(srcFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete the directory.
     * 删除目录
     * @param filePath The path of file. 文件路径
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean delete(final String filePath) {
        return delete(getFileByPath(filePath));
    }

    /**
     * Delete the directory.
     *
     * @param file The file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean delete(final File file) {
        if (file == null) {
            return false;
        }
        if (file.isDirectory()) {
            return deleteDir(file);
        }
        return deleteFile(file);
    }

    /**
     * Delete the directory.
     *
     * @param dir The directory.
     * @return {@code true}: success<br>{@code false}: fail
     */
    private static boolean deleteDir(final File dir) {
        if (dir == null) {
            return false;
        }
        /*
        * dir doesn't exist then return true
        * dir不存在，然后返回true
        * */
        if (!dir.exists()) {
            return true;
        }
        /*
        * dir isn't a directory then return false
        * 如果dir不是目录，则返回false
        * */
        if (!dir.isDirectory()) {
            return false;
        }
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.delete()) {
                        return false;
                    }
                } else if (file.isDirectory()) {
                    if (!deleteDir(file)) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

    /**
     * Delete the file.
     *
     * @param file The file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    private static boolean deleteFile(final File file) {
        return file != null && (!file.exists() || file.isFile() && file.delete());
    }

    /**
     * Delete the all in directory.
     * 删除目录中的所有内容
     * @param dirPath The path of directory. 目录的路径
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean deleteAllInDir(final String dirPath) {
        return deleteAllInDir(getFileByPath(dirPath));
    }

    /**
     * Delete the all in directory.
     *
     * @param dir The directory.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean deleteAllInDir(final File dir) {
        return deleteFilesInDirWithFilter(dir, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return true;
            }
        });
    }

    /**
     * Delete all files in directory.
     *
     * @param dirPath The path of directory.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean deleteFilesInDir(final String dirPath) {
        return deleteFilesInDir(getFileByPath(dirPath));
    }

    /**
     * Delete all files in directory.
     *
     * @param dir The directory.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean deleteFilesInDir(final File dir) {
        return deleteFilesInDirWithFilter(dir, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
    }

    /**
     * Delete all files that satisfy the filter in directory.
     *
     * @param dirPath The path of directory.
     * @param filter  The filter.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean deleteFilesInDirWithFilter(final String dirPath,
                                                     final FileFilter filter) {
        return deleteFilesInDirWithFilter(getFileByPath(dirPath), filter);
    }

    /**
     * Delete all files that satisfy the filter in directory.
     *
     * @param dir    The directory.
     * @param filter The filter.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean deleteFilesInDirWithFilter(final File dir, final FileFilter filter) {
        if (dir == null || filter == null) {
            return false;
        }
        /*
        * dir doesn't exist then return true
        * dir不存在，然后返回true
        * */
        if (!dir.exists()) {
            return true;
        }
        // dir isn't a directory then return false
        if (!dir.isDirectory()) {
            return false;
        }
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (filter.accept(file)) {
                    if (file.isFile()) {
                        if (!file.delete()) {
                            return false;
                        }
                    } else if (file.isDirectory()) {
                        if (!deleteDir(file)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Return the files in directory.
     * <p>Doesn't traverse subdirectories</p>
     * 返回目录中的文件
     * @param dirPath The path of directory. 目录的路径
     * @return the files in directory 目录中的文件
     */
    public static List<File> listFilesInDir(final String dirPath) {
        return listFilesInDir(dirPath, null);
    }

    /**
     * Return the files in directory.
     * <p>Doesn't traverse subdirectories</p>
     * 返回目录中的文件
     * @param dir The directory. 目录
     * @return the files in directory
     */
    public static List<File> listFilesInDir(final File dir) {
        return listFilesInDir(dir, null);
    }

    /**
     * Return the files in directory.
     * <p>Doesn't traverse subdirectories</p>
     *
     * @param dirPath    The path of directory.
     * @param comparator The comparator to determine the order of the list.
     * @return the files in directory
     */
    public static List<File> listFilesInDir(final String dirPath, Comparator<File> comparator) {
        return listFilesInDir(getFileByPath(dirPath), false, comparator);
    }

    /**
     * Return the files in directory.
     * <p>Doesn't traverse subdirectories</p>
     *
     * @param dir        The directory.
     * @param comparator The comparator to determine the order of the list.
     * @return the files in directory
     */
    public static List<File> listFilesInDir(final File dir, Comparator<File> comparator) {
        return listFilesInDir(dir, false, comparator);
    }

    /**
     * Return the files in directory.
     *
     * @param dirPath     The path of directory.
     * @param isRecursive True to traverse subdirectories, false otherwise.
     * @return the files in directory
     */
    public static List<File> listFilesInDir(final String dirPath, final boolean isRecursive) {
        return listFilesInDir(getFileByPath(dirPath), isRecursive);
    }

    /**
     * Return the files in directory.
     *
     * @param dir         The directory.
     * @param isRecursive True to traverse subdirectories, false otherwise.
     * @return the files in directory
     */
    public static List<File> listFilesInDir(final File dir, final boolean isRecursive) {
        return listFilesInDir(dir, isRecursive, null);
    }

    /**
     * Return the files in directory.
     *
     * @param dirPath     The path of directory.
     * @param isRecursive True to traverse subdirectories, false otherwise.
     * @param comparator  The comparator to determine the order of the list.
     * @return the files in directory
     */
    public static List<File> listFilesInDir(final String dirPath,
                                            final boolean isRecursive,
                                            final Comparator<File> comparator) {
        return listFilesInDir(getFileByPath(dirPath), isRecursive, comparator);
    }

    /**
     * Return the files in directory.
     *
     * @param dir         The directory.
     * @param isRecursive True to traverse subdirectories, false otherwise.
     * @param comparator  The comparator to determine the order of the list.
     * @return the files in directory
     */
    public static List<File> listFilesInDir(final File dir,
                                            final boolean isRecursive,
                                            final Comparator<File> comparator) {
        return listFilesInDirWithFilter(dir, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return true;
            }
        }, isRecursive, comparator);
    }

    /**
     * Return the files that satisfy the filter in directory.
     * <p>Doesn't traverse subdirectories</p>
     *
     * @param dirPath The path of directory.
     * @param filter  The filter.
     * @return the files that satisfy the filter in directory
     */
    public static List<File> listFilesInDirWithFilter(final String dirPath,
                                                      final FileFilter filter) {
        return listFilesInDirWithFilter(getFileByPath(dirPath), filter);
    }

    /**
     * Return the files that satisfy the filter in directory.
     * <p>Doesn't traverse subdirectories</p>
     *
     * @param dir    The directory.
     * @param filter The filter.
     * @return the files that satisfy the filter in directory
     */
    public static List<File> listFilesInDirWithFilter(final File dir,
                                                      final FileFilter filter) {
        return listFilesInDirWithFilter(dir, filter, false, null);
    }

    /**
     * Return the files that satisfy the filter in directory.
     * <p>Doesn't traverse subdirectories</p>
     *
     * @param dirPath    The path of directory.
     * @param filter     The filter.
     * @param comparator The comparator to determine the order of the list.
     * @return the files that satisfy the filter in directory
     */
    public static List<File> listFilesInDirWithFilter(final String dirPath,
                                                      final FileFilter filter,
                                                      final Comparator<File> comparator) {
        return listFilesInDirWithFilter(getFileByPath(dirPath), filter, comparator);
    }

    /**
     * Return the files that satisfy the filter in directory.
     * <p>Doesn't traverse subdirectories</p>
     *
     * @param dir        The directory.
     * @param filter     The filter.
     * @param comparator The comparator to determine the order of the list.
     * @return the files that satisfy the filter in directory
     */
    public static List<File> listFilesInDirWithFilter(final File dir,
                                                      final FileFilter filter,
                                                      final Comparator<File> comparator) {
        return listFilesInDirWithFilter(dir, filter, false, comparator);
    }

    /**
     * Return the files that satisfy the filter in directory.
     *
     * @param dirPath     The path of directory.
     * @param filter      The filter.
     * @param isRecursive True to traverse subdirectories, false otherwise.
     * @return the files that satisfy the filter in directory
     */
    public static List<File> listFilesInDirWithFilter(final String dirPath,
                                                      final FileFilter filter,
                                                      final boolean isRecursive) {
        return listFilesInDirWithFilter(getFileByPath(dirPath), filter, isRecursive);
    }

    /**
     * Return the files that satisfy the filter in directory.
     *
     * @param dir         The directory.
     * @param filter      The filter.
     * @param isRecursive True to traverse subdirectories, false otherwise.
     * @return the files that satisfy the filter in directory
     */
    public static List<File> listFilesInDirWithFilter(final File dir,
                                                      final FileFilter filter,
                                                      final boolean isRecursive) {
        return listFilesInDirWithFilter(dir, filter, isRecursive, null);
    }


    /**
     * Return the files that satisfy the filter in directory.
     *
     * @param dirPath     The path of directory.
     * @param filter      The filter.
     * @param isRecursive True to traverse subdirectories, false otherwise.
     * @param comparator  The comparator to determine the order of the list.
     * @return the files that satisfy the filter in directory
     */
    public static List<File> listFilesInDirWithFilter(final String dirPath,
                                                      final FileFilter filter,
                                                      final boolean isRecursive,
                                                      final Comparator<File> comparator) {
        return listFilesInDirWithFilter(getFileByPath(dirPath), filter, isRecursive, comparator);
    }

    /**
     * Return the files that satisfy the filter in directory.
     *
     * @param dir         The directory.
     * @param filter      The filter.
     * @param isRecursive True to traverse subdirectories, false otherwise.
     * @param comparator  The comparator to determine the order of the list.
     * @return the files that satisfy the filter in directory
     */
    public static List<File> listFilesInDirWithFilter(final File dir,
                                                      final FileFilter filter,
                                                      final boolean isRecursive,
                                                      final Comparator<File> comparator) {
        List<File> files = listFilesInDirWithFilterInner(dir, filter, isRecursive);
        if (comparator != null) {
            Collections.sort(files, comparator);
        }
        return files;
    }

    private static List<File> listFilesInDirWithFilterInner(final File dir,
                                                            final FileFilter filter,
                                                            final boolean isRecursive) {
        List<File> list = new ArrayList<>();
        if (!isDir(dir)) {
            return list;
        }
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (filter.accept(file)) {
                    list.add(file);
                }
                if (isRecursive && file.isDirectory()) {
                    list.addAll(listFilesInDirWithFilterInner(file, filter, true));
                }
            }
        }
        return list;
    }

    /**
     * Return the time that the file was last modified.
     * 返回文件最后一次修改的时间
     * @param filePath The path of file. 文件的路径
     * @return the time that the file was last modified 文件最后一次修改的时间
     */
    public static long getFileLastModified(final String filePath) {
        return getFileLastModified(getFileByPath(filePath));
    }

    /**
     * Return the time that the file was last modified.
     *
     * @param file The file.
     * @return the time that the file was last modified
     */
    public static long getFileLastModified(final File file) {
        if (file == null) {
            return -1;
        }
        return file.lastModified();
    }

    /**
     * Return the charset of file simply.
     *
     * @param filePath The path of file.
     * @return the charset of file simply
     */
    public static String getFileCharsetSimple(final String filePath) {
        return getFileCharsetSimple(getFileByPath(filePath));
    }

    /**
     * Return the charset of file simply.
     *
     * @param file The file.
     * @return the charset of file simply
     */
    public static String getFileCharsetSimple(final File file) {
        if (file == null) {
            return "";
        }
        if (isUtf8(file)) {
            return "UTF-8";
        }
        int p = 0;
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            p = (is.read() << 8) + is.read();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (p == 0xfffe) {
            return "Unicode";
        } else if (p == 0xfeff) {
            return "UTF-16BE";
        }
        return "GBK";
    }

    /**
     * Return whether the charset of file is utf8.
     *
     * @param filePath The path of file.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isUtf8(final String filePath) {
        return isUtf8(getFileByPath(filePath));
    }

    /**
     * Return whether the charset of file is utf8.
     *
     * @param file The file.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isUtf8(final File file) {
        if (file == null) {
            return false;
        }
        InputStream is = null;
        try {
            byte[] bytes = new byte[24];
            is = new BufferedInputStream(new FileInputStream(file));
            int read = is.read(bytes);
            if (read != -1) {
                byte[] readArr = new byte[read];
                System.arraycopy(bytes, 0, readArr, 0, read);
                return isUtf8(readArr) == 100;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * UTF-8编码方式 Utf-8 encoding mode
     * ----------------------------------------------
     * 0xxxxxxx
     * 110xxxxx 10xxxxxx
     * 1110xxxx 10xxxxxx 10xxxxxx
     * 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
     */
    private static int isUtf8(byte[] raw) {
        int i, len;
        int utf8 = 0, ascii = 0;
        if (raw.length > 3) {
            if ((raw[0] == (byte) 0xEF) && (raw[1] == (byte) 0xBB) && (raw[2] == (byte) 0xBF)) {
                return 100;
            }
        }
        len = raw.length;
        int child = 0;
        for (i = 0; i < len; ) {
            /*
            * UTF-8 byte shouldn't be FF and FE
            * UTF-8字节不应该是FF和FE
            * */
            if ((raw[i] & (byte) 0xFF) == (byte) 0xFF || (raw[i] & (byte) 0xFE) == (byte) 0xFE) {
                return 0;
            }
            if (child == 0) {
                /*
                * ASCII format is 0x0*******
                * ASCII格式是0x0******* *
                * */
                if ((raw[i] & (byte) 0x7F) == raw[i] && raw[i] != 0) {
                    ascii++;
                } else if ((raw[i] & (byte) 0xC0) == (byte) 0xC0) {
                    /*
                    * 0x11****** maybe is UTF-8
                    * 0x11****** *可能是UTF-8
                    * */
                    for (int bit = 0; bit < 8; bit++) {
                        if ((((byte) (0x80 >> bit)) & raw[i]) == ((byte) (0x80 >> bit))) {
                            child = bit;
                        } else {
                            break;
                        }
                    }
                    utf8++;
                }
                i++;
            } else {
                child = (raw.length - i > child) ? child : (raw.length - i);
                boolean currentNotUtf8 = false;
                for (int children = 0; children < child; children++) {
                    /*
                    * format must is 0x10******
                    * 格式必须是0x10******
                    * */
                    if ((raw[i + children] & ((byte) 0x80)) != ((byte) 0x80)) {
                        if ((raw[i + children] & (byte) 0x7F) == raw[i + children] && raw[i] != 0) {
                            /*
                            * ASCII format is 0x0*******
                            * ASCII格式是0x0******* *
                            * */
                            ascii++;
                        }
                        currentNotUtf8 = true;
                    }
                }
                if (currentNotUtf8) {
                    utf8--;
                    i++;
                } else {
                    utf8 += child;
                    i += child;
                }
                child = 0;
            }
        }
        /*
        * UTF-8 contains ASCII
        * utf - 8包含ASCII
        * */
        if (ascii == len) {
            return 100;
        }
        return (int) (100 * ((float) (utf8 + ascii) / (float) len));
    }

    /**
     * Return the number of lines of file.
     * 返回文件的行数
     * @param filePath The path of file. 文件路径
     * @return the number of lines of file 文件的行数
     */
    public static int getFileLines(final String filePath) {
        return getFileLines(getFileByPath(filePath));
    }

    /**
     * Return the number of lines of file.
     * 返回文件的行数
     * @param file The file. 该文件
     * @return the number of lines of file 文件的行数
     */
    public static int getFileLines(final File file) {
        int count = 1;
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024];
            int readChars;
            if (LINE_SEP.endsWith("\n")) {
                while ((readChars = is.read(buffer, 0, 1024)) != -1) {
                    for (int i = 0; i < readChars; ++i) {
                        if (buffer[i] == '\n') {
                            ++count;
                        }
                    }
                }
            } else {
                while ((readChars = is.read(buffer, 0, 1024)) != -1) {
                    for (int i = 0; i < readChars; ++i) {
                        if (buffer[i] == '\r') {
                            ++count;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    /**
     * Return the size.
     * 返回的大小
     * @param filePath The path of file. 文件路径
     * @return the size
     */
    public static String getSize(final String filePath) {
        return getSize(getFileByPath(filePath));
    }


    public static String getSize(final File file) {
        if (file == null) {
            return "";
        }
        if (file.isDirectory()) {
            return getDirSize(file);
        }
        return getFileSize(file);
    }


    private static String getDirSize(final File dir) {
        long len = getDirLength(dir);
        return len == -1 ? "" : byte2FitMemorySize(len);
    }


    @SuppressLint("DefaultLocale")
    public static String byte2FitMemorySize(final long byteSize) {
        return byte2FitMemorySize(byteSize, 3);
    }


    @SuppressLint("DefaultLocale")
    public static String byte2FitMemorySize(final long byteSize, int precision) {
        if (precision < 0) {
           // throw new IllegalArgumentException("precision shouldn't be less than zero!");
            LogUtils.e("precision shouldn't be less than zero!");
            return "";
        }
        if (byteSize < 0) {
           // throw new IllegalArgumentException("byteSize shouldn't be less than zero!");
            LogUtils.e("byteSize shouldn't be less than zero!");
            return "";
        } else if (byteSize < KB) {
            return String.format("%." + precision + "fB", (double) byteSize);
        } else if (byteSize < MB) {
            return String.format("%." + precision + "fKB", (double) byteSize / KB);
        } else if (byteSize < GB) {
            return String.format("%." + precision + "fMB", (double) byteSize / MB);
        } else {
            return String.format("%." + precision + "fGB", (double) byteSize / GB);
        }
    }


    private static String getFileSize(final File file) {
        long len = getFileLength(file);
        return len == -1 ? "" : byte2FitMemorySize(len);
    }


    public static long getLength(final String filePath) {
        return getLength(getFileByPath(filePath));
    }


    public static long getLength(final File file) {
        if (file == null) {
            return 0;
        }
        if (file.isDirectory()) {
            return getDirLength(file);
        }
        return getFileLength(file);
    }


    private static long getDirLength(final File dir) {
        if (!isDir(dir)) {
            return 0;
        }
        long len = 0;
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    len += getDirLength(file);
                } else {
                    len += file.length();
                }
            }
        }
        return len;
    }


    public static long getFileLength(final String filePath) {
        boolean isURL = filePath.matches("[a-zA-z]+://[^\\s]*");
        if (isURL) {
            try {
                HttpsURLConnection conn = (HttpsURLConnection) new URL(filePath).openConnection();
                conn.setRequestProperty("Accept-Encoding", "identity");
                conn.connect();
                if (conn.getResponseCode() == 200) {
                    return conn.getContentLength();
                }
                return -1;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return getFileLength(getFileByPath(filePath));
    }


    private static long getFileLength(final File file) {
        if (!isFile(file)) {
            return -1;
        }
        return file.length();
    }


    public static String getFileMD5ToString(final String filePath) {
        File file = StringUtils.isSpace(filePath) ? null : new File(filePath);
        return getFileMD5ToString(file);
    }


    public static String getFileMD5ToString(final File file) {
        return bytes2HexString(getFileMD5(file));
    }


    public static String bytes2HexString(final byte[] bytes) {
        return bytes2HexString(bytes, true);
    }


    public static String bytes2HexString(final byte[] bytes, boolean isUpperCase) {
        if (bytes == null) {
            return "";
        }
        char[] hexDigits = isUpperCase ? HEX_DIGITS_UPPER : HEX_DIGITS_LOWER;
        int len = bytes.length;
        if (len <= 0) {
            return "";
        }
        char[] ret = new char[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = hexDigits[bytes[i] >> 4 & 0x0f];
            ret[j++] = hexDigits[bytes[i] & 0x0f];
        }
        return new String(ret);
    }


    public static byte[] getFileMD5(final String filePath) {
        return getFileMD5(getFileByPath(filePath));
    }


    public static byte[] getFileMD5(final File file) {
        if (file == null) {
            return null;
        }
        DigestInputStream dis = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            dis = new DigestInputStream(fis, md);
            byte[] buffer = new byte[1024 * 256];
            while (true) {
                if (!(dis.read(buffer) > 0)) {
                    break;
                }
            }
            md = dis.getMessageDigest();
            return md.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Return the file's path of directory.
     * 返回文件的目录路径
     * @param file The file.
     * @return the file's path of directory 文件的目录路径
     */
    public static String getDirName(final File file) {
        if (file == null) {
            return "";
        }
        return getDirName(file.getAbsolutePath());
    }

    /**
     * Return the file's path of directory.
     * 返回文件的目录路径
     * @param filePath The path of file. 文件路径
     * @return the file's path of directory 文件的目录路径
     */
    public static String getDirName(final String filePath) {
        if (StringUtils.isSpace(filePath)) {
            return "";
        }
        int lastSep = filePath.lastIndexOf(File.separator);
        return lastSep == -1 ? "" : filePath.substring(0, lastSep + 1);
    }


    public static String getFileName(final File file) {
        if (file == null) {
            return "";
        }
        return getFileName(file.getAbsolutePath());
    }


    public static String getFileName(final String filePath) {
        if (StringUtils.isSpace(filePath)) {
            return "";
        }
        int lastSep = filePath.lastIndexOf(File.separator);
        return lastSep == -1 ? filePath : filePath.substring(lastSep + 1);
    }


    public static String getFileNameNoExtension(final File file) {
        if (file == null) {
            return "";
        }
        return getFileNameNoExtension(file.getPath());
    }


    public static String getFileNameNoExtension(final String filePath) {
        if (StringUtils.isSpace(filePath)) {
            return "";
        }
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastSep == -1) {
            return (lastPoi == -1 ? filePath : filePath.substring(0, lastPoi));
        }
        if (lastPoi == -1 || lastSep > lastPoi) {
            return filePath.substring(lastSep + 1);
        }
        return filePath.substring(lastSep + 1, lastPoi);
    }


    public static String getFileExtension(final File file) {
        if (file == null) {
            return "";
        }
        return getFileExtension(file.getPath());
    }


    public static String getFileExtension(final String filePath) {
        if (StringUtils.isSpace(filePath)) {
            return "";
        }
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) {
            return "";
        }
        return filePath.substring(lastPoi + 1);
    }

    /**
     * Notify system to scan the file.
     * 通知系统扫描文件
     * @param filePath The path of file. 文件路径
     */
    public static void notifySystemToScan(final String filePath) {
        notifySystemToScan(getFileByPath(filePath));
    }


    public static void notifySystemToScan(final File file) {
        if (file == null || !file.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.parse("file://" + file.getAbsolutePath()));
        Utils.getApp().sendBroadcast(intent);
    }


    public static long getFsTotalSize(String anyPathInFs) {
        if (TextUtils.isEmpty(anyPathInFs)) {
            return 0;
        }
        StatFs statFs = new StatFs(anyPathInFs);
        long blockSize;
        long totalSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statFs.getBlockSizeLong();
            totalSize = statFs.getBlockCountLong();
        } else {
            blockSize = statFs.getBlockSize();
            totalSize = statFs.getBlockCount();
        }
        return blockSize * totalSize;
    }


    public static long getFsAvailableSize(final String anyPathInFs) {
        if (TextUtils.isEmpty(anyPathInFs)) {
            return 0;
        }
        StatFs statFs = new StatFs(anyPathInFs);
        long blockSize;
        long availableSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statFs.getBlockSizeLong();
            availableSize = statFs.getAvailableBlocksLong();
        } else {
            blockSize = statFs.getBlockSize();
            availableSize = statFs.getAvailableBlocks();
        }
        return blockSize * availableSize;
    }

    ///////////////////////////////////////////////////////////////////////////
    // interface  接口
    ///////////////////////////////////////////////////////////////////////////


    public interface OnReplaceListener {

        boolean onReplace(File srcFile, File destFile);
    }

    /**
     * ContentRui转AbsPath
     * @param context
     * @param filePath
     * @return
     */
    public static String getAbsPathByContentUri(Context context, String filePath){
        String absPath = filePath;
        if (isContent(filePath)&&context!=null) {
            absPath = contentPath2AbsPath(context.getContentResolver()
                    , filePath);
        }
        return absPath;
    }
    public static boolean isContent(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return url.startsWith("content://");
    }
    /**
     * content路径转绝对路径
     *
     * @param contentResolver
     * @param path
     * @return
     */
    public static String contentPath2AbsPath(ContentResolver contentResolver, String path) {
        Uri uri = Uri.parse(path);
        return getFilePathFromContentUri(uri, contentResolver);
    }
    public static String getFilePathFromContentUri(Uri selectedVideoUri,
                                                   ContentResolver contentResolver) {
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};

        Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;

    }

    /**
     * ContentRui转AbsPath 带是否生效控制
     * @param context
     * @param applyFlag
     * @param filePath
     * @return
     */
    public static String getAbsPathByContentUri(Context context, boolean applyFlag, String filePath) {
        if (!applyFlag || context == null) return filePath;
        return getAbsPathByContentUri(context, filePath);
    }
}
