package com.stockmarket.service;

import java.io.IOException;

public interface IPersistable {
    /**
     * Salva os dados do sistema em um arquivo
     */
    void saveToFile(String filename) throws IOException;

    /**
     * Carrega os dados do sistema de um arquivo
     */
    void loadFromFile(String filename) throws IOException;

    /**
     * Exporta dados em formato CSV
     */
    void exportToCSV(String filename) throws IOException;

    /**
     * Importa dados de um arquivo CSV
     */
    void importFromCSV(String filename) throws IOException;

    /**
     * Verifica se o arquivo existe
     */
    boolean fileExists(String filename);
}