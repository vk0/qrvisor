package ru.dsci.qrvisor.core.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CertDto {
    private String certId;
    private String name;
    private String documentId;
    private LocalDate birthDate;
    private boolean isValid;
    private LocalDate endDate;
    private String url;

    @Override
    public String toString() {
            return String.format(
                    "СЕРТИФИКАТ ВАКЦИНАЦИИ: \n номер: %s \n ФИО: %s \n дата рождения: %s \n паспорт: %s \n сертификат действителен: %s \n срок: %s \n ГОСУСЛУГИ: %s"
                    , getCertId()
                    , getName()
                    , getBirthDate() == null ? "" : getBirthDate().toString()
                    , getDocumentId()
                    , isValid() ? "да" : "нет"
                    , getEndDate() == null ? "" : getEndDate().toString()
                    , getUrl()
            );
    }
}
