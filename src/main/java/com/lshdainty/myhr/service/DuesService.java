package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.Dues;
import com.lshdainty.myhr.domain.DuesCalcType;
import com.lshdainty.myhr.domain.DuesType;
import com.lshdainty.myhr.repository.DuesRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DuesService {
    private final MessageSource ms;
    private final DuesRepositoryImpl duesRepositoryImpl;

    @Transactional
    public Long save(String userName, int amount, DuesType type, DuesCalcType calc, String date, String detail) {
        Dues dues = Dues.createDues(userName, amount, type, calc, date, detail);
        duesRepositoryImpl.save(dues);
        return dues.getSeq();
    }

    public List<Dues> findDues() {
        return duesRepositoryImpl.findDues();
    }

    public List<Dues> findDuesByYear(String year) {
        return duesRepositoryImpl.findDuesByYear(year);
    }

    @Transactional
    public void deleteDues(Long duesSeq) {
        Dues findDues = checkDuesExist(duesSeq);
        duesRepositoryImpl.delete(findDues);
    }

    public Dues checkDuesExist(Long duesSeq) {
        Optional<Dues> dues = duesRepositoryImpl.findById(duesSeq);
        dues.orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.dues", null, null)));
        return dues.get();
    }
}
