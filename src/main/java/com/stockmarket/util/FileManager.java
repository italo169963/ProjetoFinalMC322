package com.stockmarket.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe utilitária para gerenciamento de arquivos no sistema.
 * Fornece métodos para leitura, escrita, cópia e manipulação de arquivos.
 */
public class FileManager {
    private static final String DEFAULT_DIRECTORY = "data";
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Cria o diretório padrão para dados se não existir
     */
    public static void createDataDirectory() throws IOException {
        Path dataDir = Paths.get(DEFAULT_DIRECTORY);
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }
    }

    /**
     * Obtém o caminho completo para um arquivo no diretório de dados
     */
    public static Path getDataFilePath(String filename) {
        return Paths.get(DEFAULT_DIRECTORY, filename);
    }

    /**
     * Verifica se um arquivo existe
     */
    public static boolean fileExists(String filename) {
        return Files.exists(getDataFilePath(filename));
    }

    /**
     * Lê o conteúdo de um arquivo como String
     */
    public static String readFileToString(String filename) throws IOException {
        Path filePath = getDataFilePath(filename);
        return new String(Files.readAllBytes(filePath));
    }

    /**
     * Lê um arquivo linha por linha
     */
    public static List<String> readFileLines(String filename) throws IOException {
        return Files.readAllLines(getDataFilePath(filename));
    }

    /**
     * Escreve uma String em um arquivo
     */
    public static void writeStringToFile(String filename, String content) throws IOException {
        Path filePath = getDataFilePath(filename);
        Files.write(filePath, content.getBytes());
    }

    /**
     * Escreve uma lista de linhas em um arquivo
     */
    public static void writeLinesToFile(String filename, List<String> lines) throws IOException {
        Path filePath = getDataFilePath(filename);
        Files.write(filePath, lines);
    }

    /**
     * Adiciona uma linha ao final de um arquivo
     */
    public static void appendLineToFile(String filename, String line) throws IOException {
        Path filePath = getDataFilePath(filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), true))) {
            writer.write(line);
            writer.newLine();
        }
    }

    /**
     * Cria um arquivo de backup com timestamp
     */
    public static String createBackup(String filename) throws IOException {
        Path source = getDataFilePath(filename);
        if (!Files.exists(source)) {
            throw new FileNotFoundException("Arquivo não encontrado: " + filename);
        }

        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String backupName = filename.replace(".", "_backup_" + timestamp + ".");
        Path backup = getDataFilePath(backupName);
        
        Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
        return backupName;
    }

    /**
     * Restaura um arquivo de backup
     */
    public static void restoreFromBackup(String backupName, String originalName) throws IOException {
        Path backup = getDataFilePath(backupName);
        Path original = getDataFilePath(originalName);
        
        if (!Files.exists(backup)) {
            throw new FileNotFoundException("Backup não encontrado: " + backupName);
        }
        
        Files.copy(backup, original, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Lista todos os arquivos no diretório de dados
     */
    public static List<String> listDataFiles() throws IOException {
        List<String> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(getDataFilePath(""))) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    files.add(entry.getFileName().toString());
                }
            }
        }
        return files;
    }

    /**
     * Lista arquivos de backup no diretório de dados
     */
    public static List<String> listBackupFiles() throws IOException {
        List<String> backups = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                getDataFilePath(""), "*.txt")) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                if (name.contains("_backup_")) {
                    backups.add(name);
                }
            }
        }
        return backups;
    }

    /**
     * Remove um arquivo
     */
    public static boolean deleteFile(String filename) throws IOException {
        Path filePath = getDataFilePath(filename);
        return Files.deleteIfExists(filePath);
    }

    /**
     * Limpa arquivos antigos (mais de N dias)
     */
    public static void cleanOldFiles(int days, String prefix) throws IOException {
        long cutoffTime = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L);
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                getDataFilePath(""), prefix + "*.txt")) {
            for (Path entry : stream) {
                if (Files.getLastModifiedTime(entry).toMillis() < cutoffTime) {
                    Files.delete(entry);
                }
            }
        }
    }

    /**
     * Conta o número de linhas em um arquivo
     */
    public static long countLines(String filename) throws IOException {
        Path filePath = getDataFilePath(filename);
        if (!Files.exists(filePath)) {
            return 0;
        }
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            return reader.lines().count();
        }
    }

    /**
     * Lê as últimas N linhas de um arquivo
     */
    public static List<String> readLastLines(String filename, int numLines) throws IOException {
        List<String> allLines = readFileLines(filename);
        if (allLines.size() <= numLines) {
            return allLines;
        }
        return allLines.subList(allLines.size() - numLines, allLines.size());
    }

    /**
     * Cria um arquivo com dados de exemplo para teste
     */
    public static void createSampleDataFile(String filename) throws IOException {
        List<String> lines = Arrays.asList(
            "=== USERS ===",
            "name;email;password;balance",
            "João Silva;joao@email.com;123456;10000.00",
            "Maria Santos;maria@email.com;123456;15000.00",
            "=== STOCKS ===",
            "name;symbol;price;sector;volume",
            "Alpha Tech;ALP4;150.50;Tecnologia;0",
            "Beta Bank;BET3;89.75;Financeiro;0",
            "Gamma Retail;GAM5;235.30;Varejo;0"
        );
        writeLinesToFile(filename, lines);
    }

    /**
     * Copia um arquivo para outro local
     */
    public static void copyFile(String source, String destination) throws IOException {
        Path sourcePath = getDataFilePath(source);
        Path destPath = getDataFilePath(destination);
        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Move um arquivo para outro local
     */
    public static void moveFile(String source, String destination) throws IOException {
        Path sourcePath = getDataFilePath(source);
        Path destPath = getDataFilePath(destination);
        Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Obtém o tamanho de um arquivo em bytes
     */
    public static long getFileSize(String filename) throws IOException {
        Path filePath = getDataFilePath(filename);
        return Files.size(filePath);
    }

    /**
     * Obtém a última data de modificação de um arquivo
     */
    public static LocalDateTime getLastModified(String filename) throws IOException {
        Path filePath = getDataFilePath(filename);
        return LocalDateTime.ofInstant(
            Files.getLastModifiedTime(filePath).toInstant(),
            java.time.ZoneId.systemDefault()
        );
    }

    /**
     * Verifica se o arquivo está vazio
     */
    public static boolean isEmpty(String filename) throws IOException {
        return getFileSize(filename) == 0;
    }

    /**
     * Método para ler arquivo de forma segura com tratamento de exceções
     */
    public static Optional<String> readFileSafely(String filename) {
        try {
            return Optional.of(readFileToString(filename));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Método para escrever arquivo de forma segura com tratamento de exceções
     */
    public static boolean writeFileSafely(String filename, String content) {
        try {
            writeStringToFile(filename, content);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Cria um arquivo temporário
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        return File.createTempFile(prefix, suffix);
    }

    /**
     * Converte um arquivo para String com encoding UTF-8
     */
    public static String readFileWithEncoding(String filename, String encoding) throws IOException {
        Path filePath = getDataFilePath(filename);
        return new String(Files.readAllBytes(filePath), java.nio.charset.Charset.forName(encoding));
    }

    /**
     * Escreve um arquivo com encoding específico
     */
    public static void writeFileWithEncoding(String filename, String content, String encoding) 
            throws IOException {
        Path filePath = getDataFilePath(filename);
        Files.write(filePath, content.getBytes(java.nio.charset.Charset.forName(encoding)));
    }
}