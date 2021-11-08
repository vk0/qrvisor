package ru.dsci.qrvisor.core;

import ru.dsci.qrvisor.core.dtos.CertDto;
import ru.dsci.qrvisor.core.exceptions.UserException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class CertTools {

    public static void checkCertUrl(String url) {
        if (!url.matches("(http|https)://www.gosuslugi.ru/covid-cert/verify/[\\w\\d\\?\\=\\&]*")) {
            throw new IllegalArgumentException(String.format("Invalid certificate url: %s", url));
        }
    }

    public static CertDto getCertData(String url) throws UserException {
        CertDto certDto = new CertDto();
        String certUrl = null;
        try {
            certUrl = QRTools.getCertUrlFromQR(url);
            checkCertUrl(certUrl);
            certDto.setUrl(certUrl);
            String html = URLTools.getUrlContent(certDto.getUrl());
            new CertParser(html).doParse(certDto);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            throw new UserException(String.format(
                    "Внимание! Некорректная ссылка: %s \n Возможна подделка кода!",
                    certUrl));
        } catch (IOException | RuntimeException e) {
            log.error(e.getMessage());
            if (certUrl != null)
                throw new UserException(String.format(
                        "Ошибка проверки сертификата, попробойте проверить вручную, перейдя по ссылке: %s", certUrl));
        }
        return certDto;
    }
}
