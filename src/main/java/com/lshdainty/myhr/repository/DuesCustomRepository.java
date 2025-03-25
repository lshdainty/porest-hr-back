package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.Dues;

import java.util.List;

public interface DuesCustomRepository {
    void save(Dues dues);
    Dues findById(Long id);
    List<Dues> findDues();
    List<Dues> findDuesByYear(String year);
    void delete(Dues dues);
}
