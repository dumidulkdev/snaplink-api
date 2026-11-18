package com.snaplink.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.snaplink.config.SnapLinkProperties;
import com.snaplink.mapper.LinkMapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class QrCodeService {

    private final SnapLinkProperties properties;
    private final LinkMapper linkMapper;

    public QrCodeService(SnapLinkProperties properties, LinkMapper linkMapper) {
        this.properties = properties;
        this.linkMapper = linkMapper;
    }

    public byte[] generatePng(String code, Integer requestedSize) {
        int size = requestedSize != null ? requestedSize : properties.getQr().getDefaultSize();
        if (size < properties.getQr().getMinSize() || size > properties.getQr().getMaxSize()) {
            throw new IllegalArgumentException(
                    "size must be between " + properties.getQr().getMinSize() + " and " + properties.getQr().getMaxSize());
        }
        String shortUrl = linkMapper.buildShortUrl(code);
        try {
            BitMatrix matrix = new QRCodeWriter().encode(shortUrl, BarcodeFormat.QR_CODE, size, size);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate QR code", e);
        }
    }
}