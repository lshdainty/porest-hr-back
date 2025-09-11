package com.lshdainty.myhr.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

public class PorestFile {

    private static final Logger log = LoggerFactory.getLogger(PorestFile.class);

    /**
     * 파일을 저장하는 함수.
     * MultipartFile에서 원본 파일명을 가져와서 저장합니다.
     *
     * @param multipartFile 저장할 파일
     * @param path 파일 저장 경로
     * @param ms MessageSource
     * @return boolean 저장 성공 여부
     */
    public static boolean save(MultipartFile multipartFile, String path, MessageSource ms) {
        // multipartFile이 null이거나 비어있는지 확인
        if (multipartFile == null || multipartFile.isEmpty()) {
            return false;
        }

        // 원본 파일명 가져오기 및 경로 정리 (경로 조작 방지)
        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

        // 파일명이 없는 경우 처리
        if (fileName.isEmpty()) {
            log.warn("Could not save file with empty name.");
            return false;
        }

        try {
            // 파일 저장 경로가 없을 경우 생성
            Path directory = Paths.get(path);
            Files.createDirectories(directory);

            // 파일을 저장할 전체 경로
            Path filePath = directory.resolve(fileName);

            // 파일 저장
            multipartFile.transferTo(filePath.toFile());
            return true;
        } catch (IOException e) {
            // 파일 저장 중 오류 발생 시 로그를 남기고 예외를 던집니다.
            log.error("File save failed. path: {}, fileName: {}", path, fileName, e);
            throw new RuntimeException(ms.getMessage("error.file.save", new String[]{fileName}, null), e);
        }
    }

    /**
     * 파일을 읽어오는 함수</br>
     * 파일 용량이 큰 경우 해당함수 사용금지</br>
     * OutOfMemoryError 발생 위험
     *
     * @param fullPath 읽어올 파일의 전체 경로
     * @param ms MessageSource
     * @return byte[] 파일 내용
     */
    public static byte[] read(String fullPath, MessageSource ms) {
        try {
            // 파일을 읽어올 전체 경로
            Path filePath = Paths.get(fullPath);

            // 파일이 존재하고, 일반 파일이며, 읽기 가능한지 확인
            if (Files.exists(filePath) && Files.isRegularFile(filePath) && Files.isReadable(filePath)) {
                // 파일 내용을 byte 배열로 읽어옴
                return Files.readAllBytes(filePath);
            } else {
                // 파일을 찾을 수 없을 경우 예외를 던집니다.
                log.warn("File not found. fullPath: {}", fullPath);
                throw new NoSuchElementException(ms.getMessage("error.file.notfound", new String[]{fullPath}, null));
            }
        } catch (IOException e) {
            // 파일 읽기 중 오류 발생 시 로그를 남기고 예외를 던집니다.
            log.error("File read failed. fullPath: {}", fullPath, e);
            throw new RuntimeException(ms.getMessage("error.file.read", new String[]{fullPath}, null), e);
        }
    }

    /**
     * 파일을 복사하는 함수
     *
     * @param sourcePath 원본 파일 전체 경로
     * @param targetPath 복사할 파일 전체 경로
     * @param ms MessageSource
     * @return boolean 복사 성공 여부
     */
    public static boolean copy(String sourcePath, String targetPath, MessageSource ms) {
        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);

        try {
            // 원본 파일이 존재하는지 확인
            if (!Files.exists(source)) {
                log.warn("Source file not found for copy: {}", sourcePath);
                throw new NoSuchElementException(ms.getMessage("error.file.notfound", new String[]{sourcePath}, null));
            }

            // 복사할 경로의 부모 디렉토리가 없으면 생성
            Path targetDir = target.getParent();
            if (targetDir != null && !Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // 파일 복사 (이미 파일이 존재하면 예외 발생)
            Files.copy(source, target);
            return true;
        } catch (IOException e) {
            // 파일 복사 중 오류 발생 시 로그를 남기고 예외를 던집니다.
            log.error("File copy failed. source: {}, target: {}", sourcePath, targetPath, e);
            throw new RuntimeException(ms.getMessage("error.file.copy", new String[]{sourcePath, targetPath}, null), e);
        }
    }

    /**
     * 파일을 이동하는 함수
     *
     * @param sourcePath 원본 파일 전체 경로
     * @param targetPath 이동할 파일 전체 경로
     * @param ms MessageSource
     * @return boolean 이동 성공 여부
     */
    public static boolean move(String sourcePath, String targetPath, MessageSource ms) {
        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);

        try {
            // 원본 파일이 존재하는지 확인
            if (!Files.exists(source)) {
                log.warn("Source file not found for move: {}", sourcePath);
                throw new NoSuchElementException(ms.getMessage("error.file.notfound", new String[]{sourcePath}, null));
            }

            // 이동할 경로의 부모 디렉토리가 없으면 생성
            Path targetDir = target.getParent();
            if (targetDir != null && !Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // 파일 이동
            Files.move(source, target);
            return true;
        } catch (IOException e) {
            // 파일 이동 중 오류 발생 시 로그를 남기고 예외를 던집니다.
            log.error("File move failed. source: {}, target: {}", sourcePath, targetPath, e);
            throw new RuntimeException(ms.getMessage("error.file.move", new String[]{sourcePath, targetPath}, null), e);
        }
    }
}
