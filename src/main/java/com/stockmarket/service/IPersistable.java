package com.stockmarket.service;

import java.io.IOException;

public interface IPersistable {
    void saveToFile(String filename) throws IOException;
    void loadFromFile(String filename) throws IOException;
}