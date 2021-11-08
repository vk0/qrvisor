package ru.dsci.qrvisor.core;

import ru.dsci.qrvisor.core.dtos.CertDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class CertParser {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private String srcHtml;
    private Document srcDoc;
    private Elements spanElements;
    private Elements divElements;

    public void setHtml(String srcHtml) {
        this.srcHtml = srcHtml;
        srcDoc = Jsoup.parse(srcHtml);
    }

    private LocalDate parseDate(String dateText) {
        LocalDate date = null;
        if (dateText != null) {
            try {
                date = LocalDate.parse(dateText, DATE_TIME_FORMATTER);
            } catch (RuntimeException e) {
                log.error(String.format("Date '%s' parsing error: %s", dateText), e.getMessage());
            }
        }
        return date;
    }

    private String getCertId() {
        return spanElements.get(3).text();
    }

    private String getName() {
        return divElements.get(26).text();
    }

    private String getDocumentId() {
        return divElements.get(32).text();
    }

    private LocalDate getBirthDate() {
        return parseDate(divElements.get(29).text());
    }

    public boolean getValid() {
        boolean valid = false;
        String validText = spanElements.get(1).text();
        if (validText != null && validText.equalsIgnoreCase("Действителен"))
            valid = true;
        return valid;
    }

    private LocalDate getEndDate() {
        return parseDate(divElements.get(22).text());
    }

    private void setElements() throws IOException {
        divElements = srcDoc.getElementsByTag("div");
        spanElements = srcDoc.getElementsByTag("span");
        if (spanElements.size() < 4 || divElements.size() < 34)
            throw new IOException("HTML-page has wrong format");
    }

    public CertDto doParse(CertDto certDto) throws IOException {
        if (srcHtml == null)
            throw new RuntimeException("Can't parse: You should set page content first");
        if (srcDoc == null)
            srcDoc = Jsoup.parse(srcHtml);
        if (certDto == null)
            certDto = new CertDto();
        try {
            setElements();
            certDto.setCertId(getCertId());
            certDto.setName(getName());
            certDto.setDocumentId(getDocumentId());
            certDto.setValid(getValid());
            certDto.setBirthDate(getBirthDate());
            certDto.setEndDate(getEndDate());
        } catch (RuntimeException | IOException e) {
            throw new IOException(String.format("Can't parse HTML-data: %s", e.getMessage()));
        }
        return certDto;
    }

    public CertParser(String page) {
        setHtml(page);
    }

}
