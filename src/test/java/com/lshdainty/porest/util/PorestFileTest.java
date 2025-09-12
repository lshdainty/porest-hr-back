package com.lshdainty.porest.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("porest File Util 테스트")
class PorestFileTest {

    @Mock
    private MessageSource ms;

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("파일 저장 - 성공 (커스텀 파일명)")
    void saveSuccessWithCustomFileName() throws IOException {
        // Given
        String content = "Hello World";
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "original.txt", "text/plain", content.getBytes()
        );
        String customFileName = "custom.txt";

        // When
        boolean result = PorestFile.save(multipartFile, tempDir.toString(), customFileName, ms);

        // Then
        assertThat(result).isTrue();
        Path savedFile = tempDir.resolve(customFileName);
        assertThat(Files.exists(savedFile)).isTrue();
        assertThat(Files.readString(savedFile)).isEqualTo(content);
    }

    @Test
    @DisplayName("파일 저장 - 성공 (원본 파일명)")
    void saveSuccessWithOriginalFileName() throws IOException {
        // Given
        String content = "Hello World";
        String originalFileName = "original.txt";
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", originalFileName, "text/plain", content.getBytes()
        );

        // When
        boolean result = PorestFile.save(multipartFile, tempDir.toString(), null, ms);

        // Then
        assertThat(result).isTrue();
        Path savedFile = tempDir.resolve(originalFileName);
        assertThat(Files.exists(savedFile)).isTrue();
        assertThat(Files.readString(savedFile)).isEqualTo(content);
    }

    @Test
    @DisplayName("파일 저장 - 성공 (디렉토리 자동 생성)")
    void saveSuccessCreateDirectories() throws IOException {
        // Given
        String content = "Hello World";
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", content.getBytes()
        );
        Path subDir = tempDir.resolve("sub").resolve("dir");
        String fileName = "test.txt";

        // When
        boolean result = PorestFile.save(multipartFile, subDir.toString(), fileName, ms);

        // Then
        assertThat(result).isTrue();
        Path savedFile = subDir.resolve(fileName);
        assertThat(Files.exists(savedFile)).isTrue();
        assertThat(Files.readString(savedFile)).isEqualTo(content);
    }

    @Test
    @DisplayName("파일 저장 - 실패 (null 파일)")
    void saveFailNullFile() {
        // Given & When & Then
        assertThat(PorestFile.save(null, tempDir.toString(), "test.txt", ms)).isFalse();
    }

    @Test
    @DisplayName("파일 저장 - 실패 (빈 파일)")
    void saveFailEmptyFile() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        // When & Then
        assertThat(PorestFile.save(emptyFile, tempDir.toString(), "test.txt", ms)).isFalse();
    }

    @Test
    @DisplayName("파일 저장 - 실패 (빈 파일명)")
    void saveFailEmptyFileName() {
        // Given
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "", "text/plain", "content".getBytes()
        );

        // When & Then
        assertThat(PorestFile.save(multipartFile, tempDir.toString(), null, ms)).isFalse();
    }

    @Test
    @DisplayName("파일 저장 - 실패 (IOException 발생)")
    void saveFailIOException() throws IOException {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        given(mockFile.isEmpty()).willReturn(false);

        // transferTo 메서드가 호출될 때 IOException을 던지도록 설정
        doThrow(new IOException("Transfer failed")).when(mockFile).transferTo(any(File.class));

        given(ms.getMessage("error.file.save", new String[]{"test.txt"}, null))
                .willReturn("File save failed");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> PorestFile.save(mockFile, tempDir.toString(), "test.txt", ms));

        assertThat(exception.getMessage()).isEqualTo("File save failed");
        assertThat(exception.getCause()).isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("파일 읽기 - 성공")
    void readFileSuccessTest() throws IOException {
        // Given
        String content = "Hello World";
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, content);

        // When
        byte[] result = PorestFile.read(testFile.toString(), ms);

        // Then
        assertThat(new String(result)).isEqualTo(content);
    }

    @Test
    @DisplayName("파일 읽기 - 실패 (파일 존재하지 않음)")
    void readFileFailNotFound() {
        // Given
        String nonExistentPath = tempDir.resolve("nonexistent.txt").toString();
        given(ms.getMessage("error.file.notfound", new String[]{nonExistentPath}, null))
                .willReturn("File not found");

        // When & Then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> PorestFile.read(nonExistentPath, ms));

        assertThat(exception.getMessage()).isEqualTo("File not found");
    }

    @Test
    @DisplayName("파일 읽기 - 실패 (디렉토리인 경우)")
    void readFileFailDirectory() throws IOException {
        // Given
        Path directory = tempDir.resolve("testDir");
        Files.createDirectory(directory);

        given(ms.getMessage("error.file.notfound", new String[]{directory.toString()}, null))
                .willReturn("File not found");

        // When & Then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> PorestFile.read(directory.toString(), ms));

        assertThat(exception.getMessage()).isEqualTo("File not found");
    }

    @Test
    @DisplayName("파일 읽기 - 실패 (IOException 발생 시뮬레이션)")
    void readFileFailIOException() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "content");

        // 파일을 읽기 전용으로 만들거나 삭제하여 IOException 유발
        Files.delete(testFile);
        Files.createDirectory(testFile); // 파일 대신 디렉토리 생성하여 읽기 실패 유발

        given(ms.getMessage("error.file.notfound", new String[]{testFile.toString()}, null))
                .willReturn("File not found");

        // When & Then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> PorestFile.read(testFile.toString(), ms));

        assertThat(exception.getMessage()).isEqualTo("File not found");
    }

    @Test
    @DisplayName("파일 복사 - 성공")
    void copyFileSuccessTest() throws IOException {
        // Given
        String content = "Hello World";
        Path sourceFile = tempDir.resolve("source.txt");
        Path targetFile = tempDir.resolve("target.txt");
        Files.writeString(sourceFile, content);

        // When
        boolean result = PorestFile.copy(sourceFile.toString(), targetFile.toString(), ms);

        // Then
        assertThat(result).isTrue();
        assertThat(Files.exists(sourceFile)).isTrue(); // 원본 파일은 유지
        assertThat(Files.exists(targetFile)).isTrue();
        assertThat(Files.readString(targetFile)).isEqualTo(content);
    }

    @Test
    @DisplayName("파일 복사 - 성공 (대상 디렉토리 자동 생성)")
    void copyFileSuccessCreateTargetDir() throws IOException {
        // Given
        String content = "Hello World";
        Path sourceFile = tempDir.resolve("source.txt");
        Path targetDir = tempDir.resolve("sub").resolve("dir");
        Path targetFile = targetDir.resolve("target.txt");
        Files.writeString(sourceFile, content);

        // When
        boolean result = PorestFile.copy(sourceFile.toString(), targetFile.toString(), ms);

        // Then
        assertThat(result).isTrue();
        assertThat(Files.exists(targetDir)).isTrue();
        assertThat(Files.exists(targetFile)).isTrue();
        assertThat(Files.readString(targetFile)).isEqualTo(content);
    }

    @Test
    @DisplayName("파일 복사 - 실패 (원본 파일 존재하지 않음)")
    void copyFileFailSourceNotFound() {
        // Given
        String nonExistentSource = tempDir.resolve("nonexistent.txt").toString();
        String targetPath = tempDir.resolve("target.txt").toString();
        given(ms.getMessage("error.file.notfound", new String[]{nonExistentSource}, null))
                .willReturn("Source file not found");

        // When & Then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> PorestFile.copy(nonExistentSource, targetPath, ms));

        assertThat(exception.getMessage()).isEqualTo("Source file not found");
    }

    @Test
    @DisplayName("파일 복사 - 실패 (파일이 이미 존재하는 경우)")
    void copyFileFailFileAlreadyExists() throws IOException {
        // Given
        String content = "Hello World";
        Path sourceFile = tempDir.resolve("source.txt");
        Path targetFile = tempDir.resolve("target.txt");
        Files.writeString(sourceFile, content);
        Files.writeString(targetFile, "existing content"); // 대상 파일이 이미 존재

        given(ms.getMessage("error.file.copy", new String[]{sourceFile.toString(), targetFile.toString()}, null))
                .willReturn("File copy failed");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> PorestFile.copy(sourceFile.toString(), targetFile.toString(), ms));

        assertThat(exception.getMessage()).isEqualTo("File copy failed");
        assertThat(exception.getCause()).isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("파일 이동 - 성공")
    void moveFileSuccessTest() throws IOException {
        // Given
        String content = "Hello World";
        Path sourceFile = tempDir.resolve("source.txt");
        Path targetFile = tempDir.resolve("target.txt");
        Files.writeString(sourceFile, content);

        // When
        boolean result = PorestFile.move(sourceFile.toString(), targetFile.toString(), ms);

        // Then
        assertThat(result).isTrue();
        assertThat(Files.exists(sourceFile)).isFalse(); // 원본 파일은 제거됨
        assertThat(Files.exists(targetFile)).isTrue();
        assertThat(Files.readString(targetFile)).isEqualTo(content);
    }

    @Test
    @DisplayName("파일 이동 - 성공 (대상 디렉토리 자동 생성)")
    void moveFileSuccessCreateTargetDir() throws IOException {
        // Given
        String content = "Hello World";
        Path sourceFile = tempDir.resolve("source.txt");
        Path targetDir = tempDir.resolve("sub").resolve("dir");
        Path targetFile = targetDir.resolve("target.txt");
        Files.writeString(sourceFile, content);

        // When
        boolean result = PorestFile.move(sourceFile.toString(), targetFile.toString(), ms);

        // Then
        assertThat(result).isTrue();
        assertThat(Files.exists(targetDir)).isTrue();
        assertThat(Files.exists(sourceFile)).isFalse();
        assertThat(Files.exists(targetFile)).isTrue();
        assertThat(Files.readString(targetFile)).isEqualTo(content);
    }

    @Test
    @DisplayName("파일 이동 - 실패 (원본 파일 존재하지 않음)")
    void moveFileFailSourceNotFound() {
        // Given
        String nonExistentSource = tempDir.resolve("nonexistent.txt").toString();
        String targetPath = tempDir.resolve("target.txt").toString();
        given(ms.getMessage("error.file.notfound", new String[]{nonExistentSource}, null))
                .willReturn("Source file not found");

        // When & Then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> PorestFile.move(nonExistentSource, targetPath, ms));

        assertThat(exception.getMessage()).isEqualTo("Source file not found");
    }

    @Test
    @DisplayName("파일 이동 - 실패 (파일이 이미 존재하는 경우)")
    void moveFileFailFileAlreadyExists() throws IOException {
        // Given
        String content = "Hello World";
        Path sourceFile = tempDir.resolve("source.txt");
        Path targetFile = tempDir.resolve("target.txt");
        Files.writeString(sourceFile, content);
        Files.writeString(targetFile, "existing content"); // 대상 파일이 이미 존재

        given(ms.getMessage("error.file.move", new String[]{sourceFile.toString(), targetFile.toString()}, null))
                .willReturn("File move failed");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> PorestFile.move(sourceFile.toString(), targetFile.toString(), ms));

        assertThat(exception.getMessage()).isEqualTo("File move failed");
        assertThat(exception.getCause()).isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("물리적 파일명 생성 - 성공 (확장자 있음)")
    void generatePhysicalFilenameSuccessWithExtension() {
        // Given
        String originalFilename = "document.txt";
        String uuid = "550e8400-e29b-41d4-a716-446655440000";

        // When
        String result = PorestFile.generatePhysicalFilename(originalFilename, uuid);

        // Then
        assertThat(result).isEqualTo("document_550e8400-e29b-41d4-a716-446655440000.txt");
    }

    @Test
    @DisplayName("물리적 파일명 생성 - 성공 (확장자 없음)")
    void generatePhysicalFilenameSuccessWithoutExtension() {
        // Given
        String originalFilename = "document";
        String uuid = "550e8400-e29b-41d4-a716-446655440000";

        // When
        String result = PorestFile.generatePhysicalFilename(originalFilename, uuid);

        // Then
        assertThat(result).isEqualTo("document_550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    @DisplayName("물리적 파일명 생성 - 실패 (null 파일명)")
    void generatePhysicalFilenameFailNullFilename() {
        // Given & When & Then
        assertThat(PorestFile.generatePhysicalFilename(null, "uuid")).isNull();
    }

    @Test
    @DisplayName("물리적 파일명 생성 - 실패 (빈 파일명)")
    void generatePhysicalFilenameFailEmptyFilename() {
        // Given & When & Then
        assertThat(PorestFile.generatePhysicalFilename("", "uuid")).isEqualTo("");
    }

    @Test
    @DisplayName("물리적 파일명 생성 - 실패 (null UUID)")
    void generatePhysicalFilenameFailNullUuid() {
        // Given
        String originalFilename = "document.txt";

        // When & Then
        assertThat(PorestFile.generatePhysicalFilename(originalFilename, null))
                .isEqualTo(originalFilename);
    }

    @Test
    @DisplayName("물리적 파일명 생성 - 실패 (빈 UUID)")
    void generatePhysicalFilenameFailEmptyUuid() {
        // Given
        String originalFilename = "document.txt";

        // When & Then
        assertThat(PorestFile.generatePhysicalFilename(originalFilename, ""))
                .isEqualTo(originalFilename);
    }

    @Test
    @DisplayName("원본 파일명 추출 - 성공 (UUID 제공)")
    void extractOriginalFilenameSuccessWithUuid() {
        // Given
        String physicalFilename = "document_550e8400-e29b-41d4-a716-446655440000.txt";
        String uuid = "550e8400-e29b-41d4-a716-446655440000";

        // When
        String result = PorestFile.extractOriginalFilename(physicalFilename, uuid);

        // Then
        assertThat(result).isEqualTo("document.txt");
    }

    @Test
    @DisplayName("원본 파일명 추출 - 성공 (UUID 패턴 추출)")
    void extractOriginalFilenameSuccessWithoutUuid() {
        // Given
        String physicalFilename = "document_550e8400-e29b-41d4-a716-446655440000.txt";

        // When
        String result = PorestFile.extractOriginalFilename(physicalFilename, null);

        // Then
        assertThat(result).isEqualTo("document.txt");
    }

    @Test
    @DisplayName("원본 파일명 추출 - UUID가 없는 경우")
    void extractOriginalFilenameNoUuid() {
        // Given
        String physicalFilename = "document.txt";

        // When
        String result = PorestFile.extractOriginalFilename(physicalFilename, null);

        // Then
        assertThat(result).isEqualTo("document.txt");
    }

    @Test
    @DisplayName("원본 파일명 추출 - 실패 (null 파일명)")
    void extractOriginalFilenameFailNull() {
        // Given & When & Then
        assertThat(PorestFile.extractOriginalFilename(null, "uuid")).isNull();
    }

    @Test
    @DisplayName("원본 파일명 추출 - 실패 (빈 파일명)")
    void extractOriginalFilenameFailEmpty() {
        // Given & When & Then
        assertThat(PorestFile.extractOriginalFilename("", "uuid")).isNull();
    }

    @Test
    @DisplayName("UUID 추출 - 성공 (UUID 제공)")
    void extractUuidSuccessWithUuid() {
        // Given
        String physicalFilename = "document_550e8400-e29b-41d4-a716-446655440000.txt";
        String uuid = "550e8400-e29b-41d4-a716-446655440000";

        // When
        String result = PorestFile.extractUuid(physicalFilename, uuid);

        // Then
        assertThat(result).isEqualTo(uuid);
    }

    @Test
    @DisplayName("UUID 추출 - 성공 (패턴으로 추출 - 확장자 있음)")
    void extractUuidSuccessPatternWithExtension() {
        // Given
        String physicalFilename = "document_550e8400-e29b-41d4-a716-446655440000.txt";

        // When
        String result = PorestFile.extractUuid(physicalFilename, null);

        // Then
        assertThat(result).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    @DisplayName("UUID 추출 - 성공 (패턴으로 추출 - 확장자 없음)")
    void extractUuidSuccessPatternWithoutExtension() {
        // Given
        String physicalFilename = "document_550e8400-e29b-41d4-a716-446655440000";

        // When
        String result = PorestFile.extractUuid(physicalFilename, null);

        // Then
        assertThat(result).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    @DisplayName("UUID 추출 - UUID가 없는 경우")
    void extractUuidNoUuid() {
        // Given
        String physicalFilename = "document.txt";

        // When
        String result = PorestFile.extractUuid(physicalFilename, null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("UUID 추출 - 실패 (null 파일명)")
    void extractUuidFailNull() {
        // Given & When & Then
        assertThat(PorestFile.extractUuid(null, null)).isNull();
    }

    @Test
    @DisplayName("UUID 추출 - 실패 (빈 파일명)")
    void extractUuidFailEmpty() {
        // Given & When & Then
        assertThat(PorestFile.extractUuid("", null)).isNull();
    }

    @Test
    @DisplayName("UUID 추출 - 언더스코어가 없는 경우")
    void extractUuidNoUnderscore() {
        // Given
        String physicalFilename = "document.txt";

        // When
        String result = PorestFile.extractUuid(physicalFilename, null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("UUID 추출 - 언더스코어가 마지막에 있는 경우")
    void extractUuidUnderscoreAtEnd() {
        // Given
        String physicalFilename = "document_.txt";

        // When
        String result = PorestFile.extractUuid(physicalFilename, null);

        // Then
        assertThat(result).isNull();
    }
}