package com.meishe.base.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2016/08/27
 *     desc  : utils about zip
 * </pre>
 * 压缩包工具类
 * Zip package utility class
 */
public final class ZipUtils {

    private static final int BUFFER_LEN = 8192;

    private ZipUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * Zip the files.
     * Zip文件
     * @param srcFiles    The source of files. 文件的来源
     * @param zipFilePath The path of ZIP file. ZIP文件的路径
     * @return {@code true}: success<br>{@code false}: fail
     * @throws IOException if an I/O error has occurred
     */
    public static boolean zipFiles(final Collection<String> srcFiles,
                                   final String zipFilePath)
            throws IOException {
        return zipFiles(srcFiles, zipFilePath, null);
    }

    public static boolean zipFiles(final Collection<String> srcFilePaths,
                                   final String zipFilePath,
                                   final String comment)
            throws IOException {
        if (srcFilePaths == null || zipFilePath == null) return false;
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFilePath));
            for (String srcFile : srcFilePaths) {
                if (!zipFile(FileUtils.getFileByPath(srcFile), "", zos, comment)) return false;
            }
            return true;
        } finally {
            if (zos != null) {
                zos.finish();
                zos.close();
            }
        }
    }

    /**
     * Zip the files.
     *
     * @param srcFiles The source of files.
     * @param zipFile  The ZIP file.
     * @return {@code true}: success<br>{@code false}: fail
     * @throws IOException if an I/O error has occurred
     */
    public static boolean zipFiles(final Collection<File> srcFiles, final File zipFile)
            throws IOException {
        return zipFiles(srcFiles, zipFile, null);
    }

    /**
     * Zip the files.
     *
     * @param srcFiles The source of files.
     * @param zipFile  The ZIP file.
     * @param comment  The comment.
     * @return {@code true}: success<br>{@code false}: fail
     * @throws IOException if an I/O error has occurred
     */
    public static boolean zipFiles(final Collection<File> srcFiles,
                                   final File zipFile,
                                   final String comment)
            throws IOException {
        if (srcFiles == null || zipFile == null) {
            return false;
        }
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            for (File srcFile : srcFiles) {
                if (!zipFile(srcFile, "", zos, comment)) {
                    return false;
                }
            }
            return true;
        } finally {
            if (zos != null) {
                zos.finish();
                zos.close();
            }
        }
    }

    /**
     * Zip the file.
     *
     * @param srcFilePath The path of source file.
     * @param zipFilePath The path of ZIP file.
     * @return {@code true}: success<br>{@code false}: fail
     * @throws IOException if an I/O error has occurred
     */
    public static boolean zipFile(final String srcFilePath,
                                  final String zipFilePath)
            throws IOException {
        return zipFile(FileUtils.getFileByPath(srcFilePath), FileUtils.getFileByPath(zipFilePath), null);
    }

    /**
     * Zip the file.
     *
     * @param srcFilePath The path of source file.
     * @param zipFilePath The path of ZIP file.
     * @param comment     The comment.
     * @return {@code true}: success<br>{@code false}: fail
     * @throws IOException if an I/O error has occurred
     */
    public static boolean zipFile(final String srcFilePath,
                                  final String zipFilePath,
                                  final String comment)
            throws IOException {
        return zipFile(FileUtils.getFileByPath(srcFilePath), FileUtils.getFileByPath(zipFilePath), comment);
    }

    /**
     * Zip the file.
     *
     * @param srcFile The source of file.
     * @param zipFile The ZIP file.
     * @return {@code true}: success<br>{@code false}: fail
     * @throws IOException if an I/O error has occurred
     */
    public static boolean zipFile(final File srcFile,
                                  final File zipFile)
            throws IOException {
        return zipFile(srcFile, zipFile, null);
    }

    /**
     * Zip the file.
     *
     * @param srcFile The source of file.
     * @param zipFile The ZIP file.
     * @param comment The comment.
     * @return {@code true}: success<br>{@code false}: fail
     * @throws IOException if an I/O error has occurred
     */
    public static boolean zipFile(final File srcFile,
                                  final File zipFile,
                                  final String comment)
            throws IOException {
        if (srcFile == null || zipFile == null) {
            return false;
        }
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            return zipFile(srcFile, "", zos, comment);
        } finally {
            if (zos != null) {
                zos.close();
            }
        }
    }

    private static boolean zipFile(final File srcFile,
                                   String rootPath,
                                   final ZipOutputStream zos,
                                   final String comment)
            throws IOException {
        rootPath = rootPath + (StringUtils.isSpace(rootPath) ? "" : File.separator) + srcFile.getName();
        if (srcFile.isDirectory()) {
            File[] fileList = srcFile.listFiles();
            if (fileList == null || fileList.length <= 0) {
                ZipEntry entry = new ZipEntry(rootPath + '/');
                entry.setComment(comment);
                zos.putNextEntry(entry);
                zos.closeEntry();
            } else {
                for (File file : fileList) {
                    if (!zipFile(file, rootPath, zos, comment)) {
                        return false;
                    }
                }
            }
        } else {
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(srcFile));
                ZipEntry entry = new ZipEntry(rootPath);
                entry.setComment(comment);
                zos.putNextEntry(entry);
                byte buffer[] = new byte[BUFFER_LEN];
                int len;
                while ((len = is.read(buffer, 0, BUFFER_LEN)) != -1) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        return true;
    }

    /**
     * Unzip the file.
     * 解压文件
     * @param zipFilePath The path of ZIP file. ZIP文件的路径
     * @param destDirPath The path of destination directory. 目标目录的路径
     * @return the unzipped files 解压缩文件
     * @throws IOException if unzip unsuccessfully 如果解压失败
     */
    public static List<File> unzipFile(final String zipFilePath,
                                       final String destDirPath)
            throws IOException {
        return unzipFileByKeyword(zipFilePath, destDirPath, null);
    }

    /**
     * Unzip the file.
     *
     * @param zipFile The ZIP file.
     * @param destDir The destination directory.
     * @return the unzipped files
     * @throws IOException if unzip unsuccessfully
     */
    public static List<File> unzipFile(final File zipFile,
                                       final File destDir)
            throws IOException {
        return unzipFileByKeyword(zipFile, destDir, null);
    }

    /**
     * Unzip the file by keyword.
     *
     * @param zipFilePath The path of ZIP file.
     * @param destDirPath The path of destination directory.
     * @param keyword     The keyboard.
     * @return the unzipped files
     * @throws IOException if unzip unsuccessfully
     */
    public static List<File> unzipFileByKeyword(final String zipFilePath,
                                                final String destDirPath,
                                                final String keyword)
            throws IOException {
        return unzipFileByKeyword(FileUtils.getFileByPath(zipFilePath), FileUtils.getFileByPath(destDirPath), keyword);
    }

    /**
     * Unzip the file by keyword.
     *
     * @param zipFile The ZIP file.
     * @param destDir The destination directory.
     * @param keyword The keyboard.
     * @return the unzipped files
     * @throws IOException if unzip unsuccessfully
     */
    public static List<File> unzipFileByKeyword(final File zipFile,
                                                final File destDir,
                                                final String keyword)
            throws IOException {
        if (zipFile == null || destDir == null) {
            return null;
        }
        List<File> files = new ArrayList<>();
        ZipFile zip = new ZipFile(zipFile);
        Enumeration<?> entries = zip.entries();
        try {
            if (StringUtils.isSpace(keyword)) {
                while (entries.hasMoreElements()) {
                    ZipEntry entry = ((ZipEntry) entries.nextElement());
                    String entryName = entry.getName().replace("\\", "/");
                    if (entryName.contains("../")) {
                        Log.e("ZipUtils", "entryName: " + entryName + " is dangerous!");
                        continue;
                    }
                    if (!unzipChildFile(destDir, files, zip, entry, entryName)) {
                        return files;
                    }
                }
            } else {
                while (entries.hasMoreElements()) {
                    ZipEntry entry = ((ZipEntry) entries.nextElement());
                    String entryName = entry.getName().replace("\\", "/");
                    if (entryName.contains("../")) {
                        Log.e("ZipUtils", "entryName: " + entryName + " is dangerous!");
                        continue;
                    }
                    if (entryName.contains(keyword)) {
                        if (!unzipChildFile(destDir, files, zip, entry, entryName)) {
                            return files;
                        }
                    }
                }
            }
        } finally {
            zip.close();
        }
        return files;
    }

    private static boolean unzipChildFile(final File destDir,
                                          final List<File> files,
                                          final ZipFile zip,
                                          final ZipEntry entry,
                                          final String name) throws IOException {
        File file = new File(destDir, name);
        files.add(file);
        if (entry.isDirectory()) {
            return FileUtils.createOrExistsDir(file);
        } else {
            if (!FileUtils.createOrExistsFile(file)) {
                return false;
            }
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new BufferedInputStream(zip.getInputStream(entry));
                out = new BufferedOutputStream(new FileOutputStream(file));
                byte buffer[] = new byte[BUFFER_LEN];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
        return true;
    }

    /**
     * Return the files' path in ZIP file.
     * 返回文件的路径在ZIP文件
     * @param zipFilePath The path of ZIP file. ZIP文件的路径
     * @return the files' path in ZIP file 文件的路径在ZIP文件
     * @throws IOException if an I/O error has occurred 如果I/O错误已经发生
     */
    public static List<String> getFilesPath(final String zipFilePath)
            throws IOException {
        return getFilesPath(FileUtils.getFileByPath(zipFilePath));
    }

    /**
     * Return the files' path in ZIP file.
     *
     * @param zipFile The ZIP file.
     * @return the files' path in ZIP file
     * @throws IOException if an I/O error has occurred
     */
    public static List<String> getFilesPath(final File zipFile)
            throws IOException {
        if (zipFile == null) {
            return null;
        }
        List<String> paths = new ArrayList<>();
        ZipFile zip = new ZipFile(zipFile);
        Enumeration<?> entries = zip.entries();
        while (entries.hasMoreElements()) {
            String entryName = ((ZipEntry) entries.nextElement()).getName().replace("\\", "/");
            if (entryName.contains("../")) {
                Log.e("ZipUtils", "entryName: " + entryName + " is dangerous!");
                paths.add(entryName);
            } else {
                paths.add(entryName);
            }
        }
        zip.close();
        return paths;
    }

    /**
     * Return the files' comment in ZIP file.
     * 在ZIP文件中返回文件的注释
     * @param zipFilePath The path of ZIP file. ZIP文件的路径
     * @return the files' comment in ZIP file 文件的注释在ZIP文件中
     * @throws IOException if an I/O error has occurred 如果发生I/O错误，IOException
     */
    public static List<String> getComments(final String zipFilePath)
            throws IOException {
        return getComments(FileUtils.getFileByPath(zipFilePath));
    }

    /**
     * Return the files' comment in ZIP file.
     *
     * @param zipFile The ZIP file.
     * @return the files' comment in ZIP file
     * @throws IOException if an I/O error has occurred
     */
    public static List<String> getComments(final File zipFile)
            throws IOException {
        if (zipFile == null) {
            return null;
        }
        List<String> comments = new ArrayList<>();
        ZipFile zip = new ZipFile(zipFile);
        Enumeration<?> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = ((ZipEntry) entries.nextElement());
            comments.add(entry.getComment());
        }
        zip.close();
        return comments;
    }

    /**
     * 判断文件是否为ZIP文件
     *
     * @param filePath 文件的完整路径
     * @return 如果是ZIP文件返回true，否则返回false
     */
    public static boolean isZipFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }

        String fileName = file.getName();
        if (fileName.endsWith(".zip")) {
            return true;
        }

        return false;
    }
}
