package kz.qazaqpay.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import kz.qazaqpay.model.dto.request.QRPaymentRequest;
import kz.qazaqpay.model.dto.response.QRCodeResponse;
import kz.qazaqpay.model.dto.response.TransferResponse;
import kz.qazaqpay.model.entity.Account;
import kz.qazaqpay.model.entity.Transaction;
import kz.qazaqpay.model.entity.User;
import kz.qazaqpay.repository.AccountRepository;
import kz.qazaqpay.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QRService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;

    public QRCodeResponse generateQRCode(String accountNumber, BigDecimal amount, User user) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to account");
        }

        String qrData = String.format("QAZAQPAY:%s:%s", accountNumber, amount.toString());
        String qrCodeBase64 = generateQRCodeImage(qrData);

        return QRCodeResponse.builder()
                .qrCodeBase64(qrCodeBase64)
                .accountNumber(accountNumber)
                .amount(amount)
                .message("QR code generated successfully")
                .build();
    }

    @Transactional
    public TransferResponse payByQR(QRPaymentRequest request, User user) {
        Account fromAccount = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!fromAccount.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to account");
        }

        String[] qrParts = request.getQrData().split(":");
        if (qrParts.length != 3 || !qrParts[0].equals("QAZAQPAY")) {
            throw new RuntimeException("Invalid QR code format");
        }

        String toAccountNumber = qrParts[1];
        BigDecimal amount = new BigDecimal(qrParts[2]);

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new RuntimeException("Destination account not found"));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        boolean suspicious = fraudDetectionService.isSuspicious(amount);

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(amount)
                .type(Transaction.TransactionType.QR_PAYMENT)
                .status(Transaction.TransactionStatus.COMPLETED)
                .description("QR Code Payment")
                .suspicious(suspicious)
                .build();

        transaction = transactionRepository.save(transaction);

        return TransferResponse.builder()
                .transactionId(transaction.getTransactionId())
                .fromAccount(fromAccount.getAccountNumber())
                .toAccount(toAccount.getAccountNumber())
                .amount(amount)
                .status(transaction.getStatus().name())
                .suspicious(suspicious)
                .timestamp(transaction.getCreatedAt())
                .message("QR payment completed successfully")
                .build();
    }

    private String generateQRCodeImage(String qrData) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(qrData, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}
